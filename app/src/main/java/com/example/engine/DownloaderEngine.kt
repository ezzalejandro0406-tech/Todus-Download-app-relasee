package com.example.engine

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import android.os.PowerManager
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
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
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

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

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

    init {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "toDusDownload::DownloadWakeLock")
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "toDusDownload::WifiLock")
        } catch (_: Exception) {}
    }

    private fun acquireLocks() {
        try {
            if (wakeLock?.isHeld != true) wakeLock?.acquire(24 * 60 * 60 * 1000L)
            if (wifiLock?.isHeld != true) wifiLock?.acquire()
        } catch (_: Exception) {}
    }

    private fun releaseLocksIfIdle() {
        if (activeJobs.isEmpty()) {
            try {
                if (wakeLock?.isHeld == true) wakeLock?.release()
                if (wifiLock?.isHeld == true) wifiLock?.release()
            } catch (_: Exception) {}
        }
    }

    fun startDownload(downloadId: Long, settings: AppSettings) {
        acquireLocks()
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
        releaseLocksIfIdle()
        scope.launch(Dispatchers.IO) {
            downloadDao.markPaused(downloadId)
        }
    }

    fun cancelDownload(downloadId: Long) {
        pauseFlags[downloadId]?.set(true)
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        speedTrackers.remove(downloadId)
        releaseLocksIfIdle()
        scope.launch(Dispatchers.IO) {
            val item = downloadDao.getDownloadByIdOnce(downloadId)
            if (item != null) {
                cleanupFiles(item)
            }
            downloadDao.deleteById(downloadId)
        }
    }

    private fun cleanupFiles(item: DownloadEntity) {
        if (item.filePath.isNotEmpty()) {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
            val tdPart = File("${item.filePath}.tdpart")
            if (tdPart.exists()) tdPart.delete()
            val meta = File("${item.filePath}.tdpart.meta")
            if (meta.exists()) meta.delete()
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
                break
            }

            try {
                downloadDao.updateStatusAndSpeed(downloadId, DownloadStatus.DOWNLOADING, 0L)
                executeDownload(download, pauseFlag, tracker, settings)
                downloadDao.markCompleted(downloadId, System.currentTimeMillis())
                keepRetrying = false
            } catch (e: CancellationException) {
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
                    delay(settings.reconnectDelaySeconds * 1000L)
                } else {
                    val errMsg = e.localizedMessage ?: "La descarga falló"
                    downloadDao.markFailed(downloadId, errMsg)
                    keepRetrying = false
                }
            }
        }
        activeJobs.remove(downloadId)
        speedTrackers.remove(downloadId)
        releaseLocksIfIdle()
    }

    private suspend fun executeDownload(
        initialEntity: DownloadEntity,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        var entity = initialEntity
        val targetDir = getDownloadDirectory(settings.downloadSubfolder)

        // 1. Probing URL headers to get Content-Length, Content-Disposition, and Content-Type
        var totalSize = entity.fileSize
        var detectedFileName = entity.fileName
        var mimeType = entity.mimeType

        val probe = openConnection(entity.url)
        probe.requestMethod = "HEAD"
        probe.connectTimeout = 15000
        probe.readTimeout = 15000

        try {
            probe.connect()
            val len = probe.contentLengthLong
            if (len > 0) totalSize = len
            val type = probe.contentType
            if (!type.isNullOrEmpty()) mimeType = type

            val disposition = probe.getHeaderField("Content-Disposition")
            val parsedName = parseContentDisposition(disposition)
            if (!parsedName.isNullOrBlank()) {
                detectedFileName = parsedName
            }
        } catch (_: Exception) {
            // Some servers reject HEAD, try Range GET
            try {
                val getProbe = openConnection(entity.url)
                getProbe.setRequestProperty("Range", "bytes=0-0")
                getProbe.connectTimeout = 12000
                getProbe.readTimeout = 12000
                getProbe.connect()
                val cr = getProbe.getHeaderField("Content-Range")
                if (cr != null && cr.contains("/")) {
                    val fullLen = cr.substringAfter("/").trim().toLongOrNull()
                    if (fullLen != null && fullLen > 0) totalSize = fullLen
                }
                val disp = getProbe.getHeaderField("Content-Disposition")
                val pName = parseContentDisposition(disp)
                if (!pName.isNullOrBlank()) detectedFileName = pName
                getProbe.disconnect()
            } catch (_: Exception) {}
        } finally {
            probe.disconnect()
        }

        val baseName = detectedFileName.ifBlank { "todus_${System.currentTimeMillis()}" }
        val tdPartFile = File(targetDir, "$baseName.tdpart")
        val finalFile = File(targetDir, baseName)

        entity = entity.copy(
            fileName = baseName,
            fileSize = totalSize,
            filePath = finalFile.absolutePath,
            mimeType = mimeType
        )
        downloadDao.updateDownload(entity)

        val threads = if (entity.threadsCount > 0) entity.threadsCount else settings.maxThreads
        val supportsMultiThread = totalSize > 1024 * 512 && threads > 1

        if (supportsMultiThread) {
            downloadMultiThreaded(entity, tdPartFile, finalFile, threads, pauseFlag, tracker, settings)
        } else {
            downloadSingleThreaded(entity, tdPartFile, finalFile, pauseFlag, tracker, settings)
        }
    }

    private suspend fun downloadMultiThreaded(
        entity: DownloadEntity,
        tdPartFile: File,
        finalFile: File,
        threads: Int,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val totalSize = entity.fileSize
        val raf = RandomAccessFile(tdPartFile, "rw")
        if (raf.length() < totalSize) {
            raf.setLength(totalSize)
        }
        raf.close()

        val chunkSize = totalSize / threads
        val downloadedAccumulator = AtomicLong(entity.downloadedBytes.coerceAtLeast(0L))

        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(600)
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
                        tdPartFile,
                        startByte,
                        endByte,
                        pauseFlag,
                        tracker,
                        downloadedAccumulator,
                        settings.bufferSizeBytes
                    )
                }
            }

            chunkJobs.awaitAll()

            if (pauseFlag.get()) {
                throw CancellationException("Pausado por el usuario")
            }

            // Sniff actual extension if needed and complete
            finalizeDownload(entity, tdPartFile, finalFile)
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
        bufferSize: Int
    ) {
        var connection: HttpURLConnection? = null
        var raf: RandomAccessFile? = null
        var inputStream: InputStream? = null

        try {
            connection = openConnection(urlStr)
            connection.setRequestProperty("Range", "bytes=$startByte-$endByte")
            connection.connectTimeout = 20000
            connection.readTimeout = 20000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP chunk error: $responseCode")
            }

            raf = RandomAccessFile(file, "rw")
            raf.seek(startByte)

            inputStream = connection.inputStream
            val buffer = ByteArray(bufferSize.coerceIn(16384, 1048576))
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (pauseFlag.get()) {
                    throw CancellationException("Pausado")
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
        tdPartFile: File,
        finalFile: File,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val existingDownloaded = if (tdPartFile.exists()) tdPartFile.length() else 0L
        val connection = openConnection(entity.url)

        if (existingDownloaded > 0) {
            connection.setRequestProperty("Range", "bytes=$existingDownloaded-")
        }
        connection.connectTimeout = 20000
        connection.readTimeout = 20000
        connection.connect()

        val responseCode = connection.responseCode
        val isRangeResponse = responseCode == HttpURLConnection.HTTP_PARTIAL
        val currentOffset = if (isRangeResponse) existingDownloaded else 0L

        val raf = RandomAccessFile(tdPartFile, "rw")
        raf.seek(currentOffset)

        val downloadedAccumulator = AtomicLong(currentOffset)
        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(600)
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
                    throw CancellationException("Pausado")
                }
                raf.write(buffer, 0, read)
                tracker.recordBytes(read.toLong())
                downloadedAccumulator.addAndGet(read.toLong())
            }
            input.close()
            raf.close()
            connection.disconnect()

            finalizeDownload(entity, tdPartFile, finalFile)
        } finally {
            progressUpdater.cancel()
            try { raf.close() } catch (_: Exception) {}
            try { connection.disconnect() } catch (_: Exception) {}
        }
    }

    private suspend fun finalizeDownload(entity: DownloadEntity, tdPartFile: File, finalFile: File) {
        if (!tdPartFile.exists()) return

        // Check if filename has extension, otherwise sniff magic bytes
        var targetFile = finalFile
        if (!targetFile.name.contains(".")) {
            val detectedExt = sniffExtension(tdPartFile)
            if (!detectedExt.isNullOrEmpty()) {
                targetFile = File(finalFile.parentFile, "${finalFile.name}.$detectedExt")
            }
        }

        if (targetFile.exists()) targetFile.delete()
        tdPartFile.renameTo(targetFile)

        // Update database with final path and complete
        val updated = entity.copy(
            fileName = targetFile.name,
            filePath = targetFile.absolutePath,
            status = DownloadStatus.COMPLETED,
            downloadedBytes = targetFile.length(),
            fileSize = targetFile.length(),
            speedBytesPerSec = 0L,
            completedAt = System.currentTimeMillis()
        )
        downloadDao.updateDownload(updated)
    }

    private fun sniffExtension(file: File): String? {
        if (!file.exists() || file.length() < 16) return null
        return try {
            val header = ByteArray(64)
            FileInputStream(file).use { it.read(header) }
            val headerStr = String(header, Charsets.ISO_8859_1)

            when {
                // ZIP / APK: starts with PK\x03\x04
                header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() -> {
                    // Check if contains AndroidManifest or dex
                    "apk" // In toDus, most zipped binaries are APKs
                }
                // MP4 / MOV: contains ftyp
                headerStr.contains("ftyp") -> {
                    when {
                        headerStr.contains("M4A") -> "m4a"
                        headerStr.contains("qt") -> "mov"
                        headerStr.contains("3gp") -> "3gp"
                        else -> "mp4"
                    }
                }
                // MP3: starts with ID3 or sync frame
                headerStr.startsWith("ID3") || (header[0] == 0xFF.toByte() && (header[1].toInt() and 0xE0) == 0xE0) -> "mp3"
                // OGG: starts with OggS
                headerStr.startsWith("OggS") -> "ogg"
                // FLAC: starts with fLaC
                headerStr.startsWith("fLaC") -> "flac"
                // RIFF (AVI / WAV / WEBP)
                headerStr.startsWith("RIFF") -> {
                    when {
                        headerStr.contains("AVI ") -> "avi"
                        headerStr.contains("WAVE") -> "wav"
                        headerStr.contains("WEBP") -> "webp"
                        else -> "avi"
                    }
                }
                // Matroska MKV: \x1A\x45\xDF\xA3
                header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() -> "mkv"
                // JPEG: \xFF\xD8\xFF
                header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() -> "jpg"
                // PNG: \x89PNG
                header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() -> "png"
                // GIF: GIF8
                headerStr.startsWith("GIF8") -> "gif"
                // PDF: %PDF
                headerStr.startsWith("%PDF") -> "pdf"
                // RAR: Rar!
                headerStr.startsWith("Rar!") -> "rar"
                // 7z: 7z\xBC\xAF\x27\x1C
                header[0] == 0x37.toByte() && header[1] == 0x7A.toByte() -> "7z"
                else -> "bin"
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getDownloadDirectory(subfolder: String): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dir = if (publicDownloads != null && publicDownloads.canWrite()) {
            File(publicDownloads, subfolder)
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, subfolder)
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun parseContentDisposition(disposition: String?): String? {
        if (disposition.isNullOrBlank()) return null
        return try {
            val utf8Matcher = Pattern.compile("filename\\*=(?:UTF-8''|utf-8'')([^;]+)").matcher(disposition)
            if (utf8Matcher.find()) {
                val enc = utf8Matcher.group(1)
                return URLDecoder.decode(enc, "UTF-8")
            }
            val plainMatcher = Pattern.compile("filename=\"?([^\";]+)\"?").matcher(disposition)
            if (plainMatcher.find()) {
                return plainMatcher.group(1).trim()
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun openConnection(urlStr: String): HttpURLConnection {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; toDus Download) AppleWebKit/537.36")
        connection.setRequestProperty("Accept-Encoding", "identity")

        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = permissiveSslSocketFactory
            connection.setHostnameVerifier { _, _ -> true }
        }
        return connection
    }
}
