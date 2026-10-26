package com.example.engine

import android.content.Context
import android.os.Environment
import com.example.data.DownloadDao
import com.example.model.AppSettings
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DownloaderEngine(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val scope: CoroutineScope
) {
    private val activeJobs = ConcurrentHashMap<Long, Job>()
    private val pauseFlags = ConcurrentHashMap<Long, AtomicBoolean>()
    private val speedTrackers = ConcurrentHashMap<Long, SpeedTracker>()

    private class SpeedTracker {
        val bytesSinceLastTick = AtomicLong(0L)
        @Volatile var currentSpeedBps: Long = 0L
        @Volatile var lastSampleTime: Long = System.currentTimeMillis()

        fun recordBytes(bytes: Long) {
            bytesSinceLastTick.addAndGet(bytes)
        }

        fun sampleSpeed(): Long {
            val now = System.currentTimeMillis()
            val elapsed = now - lastSampleTime
            if (elapsed >= 500) {
                val bytes = bytesSinceLastTick.getAndSet(0L)
                currentSpeedBps = if (elapsed > 0) (bytes * 1000L) / elapsed else 0L
                lastSampleTime = now
            }
            return currentSpeedBps
        }
    }

    private val permissiveSslSocketFactory by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        sslContext.socketFactory
    }

    fun startDownload(downloadId: Long, settings: AppSettings) {
        // Cancel existing job if running
        activeJobs[downloadId]?.cancel()
        val pauseFlag = AtomicBoolean(false)
        pauseFlags[downloadId] = pauseFlag
        val tracker = SpeedTracker()
        speedTrackers[downloadId] = tracker

        val job = scope.launch(Dispatchers.IO) {
            runDownloadLoop(downloadId, pauseFlag, tracker, settings)
        }
        activeJobs[downloadId] = job
    }

    fun pauseDownload(downloadId: Long) {
        pauseFlags[downloadId]?.set(true)
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        speedTrackers.remove(downloadId)
        scope.launch(Dispatchers.IO) {
            downloadDao.markPaused(downloadId)
        }
    }

    fun cancelDownload(downloadId: Long) {
        pauseFlags[downloadId]?.set(true)
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        speedTrackers.remove(downloadId)
        scope.launch(Dispatchers.IO) {
            val item = downloadDao.getDownloadByIdOnce(downloadId)
            if (item != null && item.filePath.isNotEmpty()) {
                val file = File(item.filePath)
                if (file.exists()) file.delete()
                val partFile = File("${item.filePath}.part")
                if (partFile.exists()) partFile.delete()
            }
            downloadDao.deleteById(downloadId)
        }
    }

    private suspend fun runDownloadLoop(
        downloadId: Long,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) {
        var reconnectAttempt = 0
        var keepRetrying = true

        while (keepRetrying && !pauseFlag.get() && scope.isActive) {
            val download = downloadDao.getDownloadByIdOnce(downloadId) ?: return

            if (download.status == DownloadStatus.PAUSED || download.status == DownloadStatus.COMPLETED) {
                return
            }

            try {
                downloadDao.updateStatusAndSpeed(downloadId, DownloadStatus.DOWNLOADING, 0L)
                executeDownload(download, pauseFlag, tracker, settings)
                // If executeDownload completes successfully:
                downloadDao.markCompleted(downloadId, System.currentTimeMillis())
                keepRetrying = false
            } catch (e: CancellationException) {
                // Manually paused or canceled
                keepRetrying = false
                break
            } catch (e: Exception) {
                if (pauseFlag.get()) {
                    downloadDao.markPaused(downloadId)
                    keepRetrying = false
                    break
                }

                if (settings.autoReconnect && reconnectAttempt < settings.maxReconnectAttempts) {
                    reconnectAttempt++
                    downloadDao.markReconnecting(downloadId, reconnectAttempt)
                    // Wait before reconnecting
                    delay(settings.reconnectDelaySeconds * 1000L)
                } else {
                    val errMsg = e.localizedMessage ?: "Error de descarga"
                    downloadDao.markFailed(downloadId, errMsg)
                    keepRetrying = false
                }
            }
        }
    }

    private suspend fun executeDownload(
        initialEntity: DownloadEntity,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        var entity = initialEntity
        val targetDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir,
            settings.downloadSubfolder
        )
        if (!targetDir.exists()) targetDir.mkdirs()

        val finalFile = File(targetDir, entity.fileName)
        val partFile = File(targetDir, "${entity.fileName}.part")

        // 1. Probe header / info if size is unknown
        var totalSize = entity.fileSize
        var mimeType = entity.mimeType

        val connection = openConnection(entity.url, settings)
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        try {
            connection.connect()
            val len = connection.contentLengthLong
            if (len > 0) totalSize = len
            val type = connection.contentType
            if (!type.isNullOrEmpty()) mimeType = type
        } catch (_: Exception) {
            // Some servers reject HEAD, try GET with Range 0-0
            try {
                val getProbe = openConnection(entity.url, settings)
                getProbe.setRequestProperty("Range", "bytes=0-0")
                getProbe.connectTimeout = 10000
                getProbe.readTimeout = 10000
                getProbe.connect()
                val cr = getProbe.getHeaderField("Content-Range")
                if (cr != null && cr.contains("/")) {
                    val fullLen = cr.substringAfter("/").trim().toLongOrNull()
                    if (fullLen != null && fullLen > 0) totalSize = fullLen
                }
                getProbe.disconnect()
            } catch (_: Exception) {}
        } finally {
            connection.disconnect()
        }

        // Update database with resolved size and path
        entity = entity.copy(
            fileSize = totalSize,
            filePath = finalFile.absolutePath,
            mimeType = mimeType
        )
        downloadDao.updateDownload(entity)

        // 2. Determine chunking & multi-threading
        val threads = if (entity.threadsCount > 0) entity.threadsCount else settings.maxThreads
        val supportsMultiThread = totalSize > 1024 * 512 && threads > 1

        if (supportsMultiThread) {
            downloadMultiThreaded(entity, partFile, finalFile, threads, pauseFlag, tracker, settings)
        } else {
            downloadSingleThreaded(entity, partFile, finalFile, pauseFlag, tracker, settings)
        }
    }

    private suspend fun downloadMultiThreaded(
        entity: DownloadEntity,
        partFile: File,
        finalFile: File,
        threads: Int,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val totalSize = entity.fileSize
        val raf = RandomAccessFile(partFile, "rw")
        if (raf.length() < totalSize) {
            raf.setLength(totalSize)
        }
        raf.close()

        val chunkSize = totalSize / threads
        val downloadedAccumulator = AtomicLong(entity.downloadedBytes.coerceAtLeast(0L))

        // Progress updater loop
        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(800)
                val curSpeed = tracker.sampleSpeed()
                val currentBytes = downloadedAccumulator.get().coerceAtMost(totalSize)
                downloadDao.updateProgress(entity.id, currentBytes, curSpeed)
            }
        }

        try {
            val chunkJobs = (0 until threads).map { index ->
                val startByte = index * chunkSize
                val endByte = if (index == threads - 1) totalSize - 1 else (startByte + chunkSize - 1)

                async(Dispatchers.IO) {
                    downloadChunk(
                        entity.url,
                        partFile,
                        startByte,
                        endByte,
                        pauseFlag,
                        tracker,
                        downloadedAccumulator,
                        settings
                    )
                }
            }

            chunkJobs.awaitAll()

            if (pauseFlag.get()) {
                throw CancellationException("Download paused by user")
            }

            // Rename .part to final
            if (partFile.exists()) {
                if (finalFile.exists()) finalFile.delete()
                partFile.renameTo(finalFile)
            }
        } finally {
            progressUpdater.cancel()
        }
    }

    private fun downloadChunk(
        urlStr: String,
        file: File,
        startByte: Long,
        endByte: Long,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        downloadedAccumulator: AtomicLong,
        settings: AppSettings
    ) {
        var connection: HttpURLConnection? = null
        var raf: RandomAccessFile? = null
        var inputStream: InputStream? = null

        try {
            connection = openConnection(urlStr, settings)
            connection.setRequestProperty("Range", "bytes=$startByte-$endByte")
            connection.connectTimeout = 20000
            connection.readTimeout = 20000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP chunk error code: $responseCode")
            }

            raf = RandomAccessFile(file, "rw")
            raf.seek(startByte)

            inputStream = connection.inputStream
            val buffer = ByteArray(settings.bufferSizeBytes.coerceIn(16384, 1048576))
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (pauseFlag.get()) {
                    throw CancellationException("Chunk paused")
                }
                raf.write(buffer, 0, bytesRead)
                tracker.recordBytes(bytesRead.toLong())
                downloadedAccumulator.addAndGet(bytesRead.toLong())
            }
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { raf?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }

    private suspend fun downloadSingleThreaded(
        entity: DownloadEntity,
        partFile: File,
        finalFile: File,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val existingDownloaded = if (partFile.exists()) partFile.length() else 0L
        val connection = openConnection(entity.url, settings)

        if (existingDownloaded > 0) {
            connection.setRequestProperty("Range", "bytes=$existingDownloaded-")
        }
        connection.connectTimeout = 20000
        connection.readTimeout = 20000
        connection.connect()

        val responseCode = connection.responseCode
        val isRangeResponse = responseCode == HttpURLConnection.HTTP_PARTIAL
        val currentOffset = if (isRangeResponse) existingDownloaded else 0L

        val raf = RandomAccessFile(partFile, "rw")
        raf.seek(currentOffset)

        val downloadedAccumulator = AtomicLong(currentOffset)
        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(800)
                val curSpeed = tracker.sampleSpeed()
                downloadDao.updateProgress(entity.id, downloadedAccumulator.get(), curSpeed)
            }
        }

        try {
            val input = connection.inputStream
            val buffer = ByteArray(settings.bufferSizeBytes.coerceIn(16384, 1048576))
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                if (pauseFlag.get()) {
                    throw CancellationException("Single-thread paused")
                }
                raf.write(buffer, 0, read)
                tracker.recordBytes(read.toLong())
                downloadedAccumulator.addAndGet(read.toLong())
            }
            input.close()
            raf.close()
            connection.disconnect()

            if (partFile.exists()) {
                if (finalFile.exists()) finalFile.delete()
                partFile.renameTo(finalFile)
            }
        } finally {
            progressUpdater.cancel()
            try { raf.close() } catch (_: Exception) {}
            try { connection.disconnect() } catch (_: Exception) {}
        }
    }

    private fun openConnection(urlStr: String, settings: AppSettings): HttpURLConnection {
        val url = URL(urlStr)
        val connection = if (settings.proxyEnabled && settings.proxyHost.isNotBlank()) {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(settings.proxyHost, settings.proxyPort))
            url.openConnection(proxy) as HttpURLConnection
        } else {
            url.openConnection() as HttpURLConnection
        }

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; toDus Download) AppleWebKit/537.36")
        connection.setRequestProperty("Accept-Encoding", "identity")

        if (connection is HttpsURLConnection && settings.sslSniBypass) {
            connection.sslSocketFactory = permissiveSslSocketFactory
            connection.setHostnameVerifier { _, _ -> true }
        }

        return connection
    }
}
