package com.example.engine

import android.content.Context
import android.media.MediaScannerConnection
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
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.URL
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
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
            if (elapsed >= 400) {
                val bytes = bytesSinceLastTick.getAndSet(0L)
                currentSpeedBps = if (elapsed > 0) (bytes * 1000L) / elapsed else 0L
                lastSampleTime = now
            }
            return currentSpeedBps
        }
    }

    private val permissiveSslSocketFactory: SSLSocketFactory by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val defaultFactory = sslContext.socketFactory

        object : SSLSocketFactory() {
            override fun getDefaultCipherSuites(): Array<String> = defaultFactory.defaultCipherSuites
            override fun getSupportedCipherSuites(): Array<String> = defaultFactory.supportedCipherSuites

            override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
                val socket = defaultFactory.createSocket(s, host, port, autoClose)
                applySni(socket, host)
                return socket
            }

            override fun createSocket(host: String?, port: Int): Socket {
                val socket = defaultFactory.createSocket(host, port)
                applySni(socket, host)
                return socket
            }

            override fun createSocket(host: String?, port: Int, localHost: java.net.InetAddress?, localPort: Int): Socket {
                val socket = defaultFactory.createSocket(host, port, localHost, localPort)
                applySni(socket, host)
                return socket
            }

            override fun createSocket(host: java.net.InetAddress?, port: Int): Socket {
                val socket = defaultFactory.createSocket(host, port)
                applySni(socket, host?.hostAddress ?: host?.hostName)
                return socket
            }

            override fun createSocket(address: java.net.InetAddress?, port: Int, localAddress: java.net.InetAddress?, localPort: Int): Socket {
                val socket = defaultFactory.createSocket(address, port, localAddress, localPort)
                applySni(socket, address?.hostAddress ?: address?.hostName)
                return socket
            }

            private fun applySni(socket: Socket?, targetHost: String?) {
                if (socket is SSLSocket) {
                    try {
                        val params = socket.sslParameters
                        val sniHost = if (targetHost == "152.207.206.86" || targetHost?.contains("todus") == true) {
                            "s3.todus.cu"
                        } else {
                            targetHost
                        }
                        if (!sniHost.isNullOrEmpty()) {
                            params.serverNames = listOf(SNIHostName(sniHost))
                            socket.sslParameters = params
                        }
                    } catch (_: Exception) {}
                }
            }
        }
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

        // 1. Check if this download is a multi-part chunked download (.udyat format)
        if (entity.chunksData.isNotBlank()) {
            downloadChunkedFile(entity, targetDir, pauseFlag, tracker, settings)
            return@withContext
        }

        // 2. Direct single file download (Probing Range & Content-Length)
        var totalSize = entity.fileSize
        var detectedFileName = entity.fileName
        var mimeType = entity.mimeType
        var supportsRange = false

        val probe = openConnection(entity.url, settings)
        probe.requestMethod = "HEAD"
        probe.connectTimeout = 12000
        probe.readTimeout = 12000

        try {
            probe.connect()
            val len = probe.contentLengthLong
            if (len > 0) totalSize = len
            val type = probe.contentType
            if (!type.isNullOrEmpty()) mimeType = type

            val acceptRanges = probe.getHeaderField("Accept-Ranges")
            if (acceptRanges?.contains("bytes", ignoreCase = true) == true) {
                supportsRange = true
            }

            val disposition = probe.getHeaderField("Content-Disposition")
            val parsedName = parseContentDisposition(disposition)
            if (!parsedName.isNullOrBlank()) {
                detectedFileName = parsedName
            }
        } catch (_: Exception) {
            // Some servers reject HEAD, try Range probe
            try {
                val getProbe = openConnection(entity.url, settings)
                getProbe.setRequestProperty("Range", "bytes=0-0")
                getProbe.connectTimeout = 12000
                getProbe.readTimeout = 12000
                getProbe.connect()
                val cr = getProbe.getHeaderField("Content-Range")
                if (cr != null && cr.contains("/")) {
                    supportsRange = true
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

        val baseName = detectedFileName.ifBlank { "jk_${System.currentTimeMillis()}" }
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
        val shouldMultiThread = supportsRange && totalSize > 1024 * 512 && threads > 1

        if (shouldMultiThread) {
            downloadMultiThreadedRanges(entity, tdPartFile, finalFile, threads, pauseFlag, tracker, settings)
        } else {
            downloadSingleThreaded(entity, tdPartFile, finalFile, pauseFlag, tracker, settings)
        }
    }

    private data class ChunkPart(val url: String, val size: Long, val md5: String?)

    private suspend fun downloadChunkedFile(
        entity: DownloadEntity,
        targetDir: File,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray(entity.chunksData)
        val parts = mutableListOf<ChunkPart>()
        var calculatedTotal = 0L

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val u = obj.optString("u")
            val s = obj.optLong("s", 0L)
            val m = obj.optString("m").ifBlank { null }
            if (u.isNotBlank()) {
                parts.add(ChunkPart(u, s, m))
                calculatedTotal += s
            }
        }

        if (parts.isEmpty()) {
            throw Exception("No se encontraron fragmentos (chunks) para descargar")
        }

        val totalSize = if (entity.fileSize > 0) entity.fileSize else calculatedTotal
        val baseName = entity.fileName.ifBlank { "jk_${System.currentTimeMillis()}" }
        val tdPartFile = File(targetDir, "$baseName.tdpart")
        val finalFile = File(targetDir, baseName)

        val updatedEntity = entity.copy(
            fileName = baseName,
            fileSize = totalSize,
            filePath = finalFile.absolutePath
        )
        downloadDao.updateDownload(updatedEntity)

        // Preallocate target file length in RandomAccessFile
        val rafInit = RandomAccessFile(tdPartFile, "rw")
        if (rafInit.length() < totalSize) {
            rafInit.setLength(totalSize)
        }
        rafInit.close()

        val downloadedAccumulator = AtomicLong(entity.downloadedBytes.coerceAtLeast(0L))

        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(500)
                val curSpeed = tracker.sampleSpeed()
                val currentBytes = downloadedAccumulator.get().coerceAtMost(totalSize)
                downloadDao.updateProgress(entity.id, currentBytes, curSpeed)
            }
        }

        try {
            // Calculate starting offset for each chunk
            val offsets = LongArray(parts.size)
            var currentAcc = 0L
            for (i in parts.indices) {
                offsets[i] = currentAcc
                currentAcc += parts[i].size
            }

            // Distribute chunk downloads among thread workers
            val threads = (if (entity.threadsCount > 0) entity.threadsCount else settings.maxThreads).coerceAtLeast(1)
            val queue = java.util.concurrent.ConcurrentLinkedQueue(parts.indices.toList())

            val workers = (0 until threads).map {
                async(Dispatchers.IO) {
                    while (!queue.isEmpty() && !pauseFlag.get()) {
                        val chunkIdx = queue.poll() ?: break
                        val chunk = parts[chunkIdx]
                        val chunkOffset = offsets[chunkIdx]
                        downloadSingleChunk(
                            chunk = chunk,
                            offset = chunkOffset,
                            destFile = tdPartFile,
                            pauseFlag = pauseFlag,
                            tracker = tracker,
                            downloadedAccumulator = downloadedAccumulator,
                            settings = settings
                        )
                    }
                }
            }

            workers.awaitAll()

            if (pauseFlag.get()) {
                throw CancellationException("Pausado")
            }

            finalizeDownload(updatedEntity, tdPartFile, finalFile)
        } finally {
            progressUpdater.cancel()
        }
    }

    private fun downloadSingleChunk(
        chunk: ChunkPart,
        offset: Long,
        destFile: File,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        downloadedAccumulator: AtomicLong,
        settings: AppSettings
    ) {
        var attempts = 0
        var success = false
        var lastErr: Exception? = null

        while (attempts < settings.maxReconnectAttempts && !success && !pauseFlag.get()) {
            attempts++
            var conn: HttpURLConnection? = null
            var raf: RandomAccessFile? = null
            var input: InputStream? = null

            try {
                conn = openConnection(chunk.url, settings)
                conn.connectTimeout = 20000
                conn.readTimeout = 25000
                conn.connect()

                val code = conn.responseCode
                if (code == 404) {
                    throw Exception("El fragmento no se encuentra en la nube (HTTP 404). Ya ha sido descargado y eliminado.")
                }
                if (code !in 200..299) {
                    throw Exception("Error de red en fragmento: HTTP $code")
                }

                raf = RandomAccessFile(destFile, "rw")
                raf.seek(offset)

                input = conn.inputStream
                val bufferSize = 131072 // 128 KB buffer for optimal throughput
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int
                val md5Digest = if (!chunk.md5.isNullOrEmpty()) MessageDigest.getInstance("MD5") else null

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (pauseFlag.get()) {
                        throw CancellationException("Pausado")
                    }
                    raf.write(buffer, 0, bytesRead)
                    md5Digest?.update(buffer, 0, bytesRead)
                    tracker.recordBytes(bytesRead.toLong())
                    downloadedAccumulator.addAndGet(bytesRead.toLong())

                    applySpeedThrottle(bytesRead, settings.speedLimit)
                }

                // Verify chunk MD5 if provided
                if (md5Digest != null && !chunk.md5.isNullOrEmpty()) {
                    val digestBytes = md5Digest.digest()
                    val hex = digestBytes.joinToString("") { "%02x".format(it) }
                    if (!hex.equals(chunk.md5, ignoreCase = true)) {
                        throw Exception("Fallo de integridad MD5 en el fragmento (Esperado: ${chunk.md5}, Obtenido: $hex)")
                    }
                }

                success = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastErr = e
                try { Thread.sleep(1000L * attempts) } catch (_: Exception) {}
            } finally {
                try { input?.close() } catch (_: Exception) {}
                try { raf?.close() } catch (_: Exception) {}
                try { conn?.disconnect() } catch (_: Exception) {}
            }
        }

        if (!success && !pauseFlag.get()) {
            throw lastErr ?: Exception("Error descargando fragmento tras $attempts intentos")
        }
    }

    private suspend fun downloadMultiThreadedRanges(
        entity: DownloadEntity,
        tdPartFile: File,
        finalFile: File,
        threads: Int,
        pauseFlag: AtomicBoolean,
        tracker: SpeedTracker,
        settings: AppSettings
    ) = withContext(Dispatchers.IO) {
        val totalSize = entity.fileSize
        val rafInit = RandomAccessFile(tdPartFile, "rw")
        if (rafInit.length() < totalSize) {
            rafInit.setLength(totalSize)
        }
        rafInit.close()

        val chunkSize = totalSize / threads
        val downloadedAccumulator = AtomicLong(entity.downloadedBytes.coerceAtLeast(0L))

        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(500)
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
                    downloadRange(
                        entity.url,
                        tdPartFile,
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
                throw CancellationException("Pausado")
            }

            finalizeDownload(entity, tdPartFile, finalFile)
        } finally {
            progressUpdater.cancel()
        }
    }

    private fun downloadRange(
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
            connection.readTimeout = 25000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP chunk error: $responseCode")
            }

            raf = RandomAccessFile(file, "rw")
            raf.seek(startByte)

            inputStream = connection.inputStream
            val buffer = ByteArray(131072) // 128 KB
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (pauseFlag.get()) {
                    throw CancellationException("Pausado")
                }
                raf.write(buffer, 0, bytesRead)
                tracker.recordBytes(bytesRead.toLong())
                downloadedAccumulator.addAndGet(bytesRead.toLong())

                applySpeedThrottle(bytesRead, settings.speedLimit)
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
        val connection = openConnection(entity.url, settings)

        if (existingDownloaded > 0) {
            connection.setRequestProperty("Range", "bytes=$existingDownloaded-")
        }
        connection.connectTimeout = 20000
        connection.readTimeout = 25000
        connection.connect()

        val responseCode = connection.responseCode
        val isRangeResponse = responseCode == HttpURLConnection.HTTP_PARTIAL
        val currentOffset = if (isRangeResponse) existingDownloaded else 0L

        val raf = RandomAccessFile(tdPartFile, "rw")
        raf.seek(currentOffset)

        val downloadedAccumulator = AtomicLong(currentOffset)
        val progressUpdater = launch {
            while (isActive && !pauseFlag.get()) {
                delay(500)
                val curSpeed = tracker.sampleSpeed()
                downloadDao.updateProgress(entity.id, downloadedAccumulator.get(), curSpeed)
            }
        }

        try {
            val input = connection.inputStream
            val buffer = ByteArray(131072) // 128 KB
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                if (pauseFlag.get()) {
                    throw CancellationException("Pausado")
                }
                raf.write(buffer, 0, read)
                tracker.recordBytes(read.toLong())
                downloadedAccumulator.addAndGet(read.toLong())

                applySpeedThrottle(read, settings.speedLimit)
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

    private fun applySpeedThrottle(bytesJustRead: Int, speedLimitSetting: String) {
        if (speedLimitSetting.isBlank() || speedLimitSetting == "Sin Límite") return
        val maxBytesPerSec = when (speedLimitSetting) {
            "1 MB/s" -> 1024L * 1024L
            "2 MB/s" -> 2L * 1024L * 1024L
            "5 MB/s" -> 5L * 1024L * 1024L
            "10 MB/s" -> 10L * 1024L * 1024L
            "20 MB/s" -> 20L * 1024L * 1024L
            else -> return
        }
        val expectedNanos = (bytesJustRead.toLong() * 1_000_000_000L) / maxBytesPerSec
        val sleepMillis = expectedNanos / 1_000_000L
        if (sleepMillis > 2) {
            try { Thread.sleep(sleepMillis) } catch (_: Exception) {}
        }
    }

    private suspend fun finalizeDownload(entity: DownloadEntity, tdPartFile: File, finalFile: File) {
        if (!tdPartFile.exists()) return

        var targetFile = finalFile
        if (!targetFile.name.contains(".")) {
            val detectedExt = sniffExtension(tdPartFile)
            if (!detectedExt.isNullOrEmpty()) {
                targetFile = File(finalFile.parentFile, "${finalFile.name}.$detectedExt")
            }
        }

        if (targetFile.exists()) targetFile.delete()
        tdPartFile.renameTo(targetFile)

        // Broadcast to Android MediaStore so file appears in gallery/downloads immediately
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                null,
                null
            )
        } catch (_: Exception) {}

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
                // ZIP / APK: PK\x03\x04
                header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() -> "apk"
                headerStr.contains("ftyp") -> {
                    when {
                        headerStr.contains("M4A") -> "m4a"
                        headerStr.contains("qt") -> "mov"
                        headerStr.contains("3gp") -> "3gp"
                        else -> "mp4"
                    }
                }
                headerStr.startsWith("ID3") || (header[0] == 0xFF.toByte() && (header[1].toInt() and 0xE0) == 0xE0) -> "mp3"
                headerStr.startsWith("OggS") -> "ogg"
                headerStr.startsWith("fLaC") -> "flac"
                headerStr.startsWith("RIFF") -> {
                    when {
                        headerStr.contains("AVI ") -> "avi"
                        headerStr.contains("WAVE") -> "wav"
                        headerStr.contains("WEBP") -> "webp"
                        else -> "avi"
                    }
                }
                header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() -> "mkv"
                header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() -> "jpg"
                header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() -> "png"
                headerStr.startsWith("GIF8") -> "gif"
                headerStr.startsWith("%PDF") -> "pdf"
                headerStr.startsWith("Rar!") -> "rar"
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

    private fun openConnection(urlStr: String, settings: AppSettings? = null): HttpURLConnection {
        // En Cuba con 0 megas (navegación nacional ETECSA gratuita), la resolución DNS de s3.todus.cu
        // falla con UnknownHostException. Se reemplaza 's3.todus.cu' directamente por la IP nacional
        // '152.207.206.86' (método idéntico a Ls30;->g y Lmf0;->a de la APK de referencia).
        val resolvedUrlStr = if (urlStr.contains("s3.todus.cu", ignoreCase = true)) {
            urlStr.replace("s3.todus.cu", "152.207.206.86", ignoreCase = true)
        } else {
            urlStr
        }

        val url = URL(resolvedUrlStr)
        val connection = if (settings?.proxyEnabled == true && settings.proxyHost.isNotBlank()) {
            try {
                val proxy = Proxy(
                    Proxy.Type.HTTP,
                    InetSocketAddress(settings.proxyHost, settings.proxyPort)
                )
                url.openConnection(proxy) as HttpURLConnection
            } catch (_: Exception) {
                url.openConnection() as HttpURLConnection
            }
        } else {
            url.openConnection() as HttpURLConnection
        }

        connection.instanceFollowRedirects = true
        connection.connectTimeout = 20000
        connection.readTimeout = 25000

        // Exact User-Agent and Headers matching reference app:
        val lowerUrl = urlStr.lowercase()
        if (lowerUrl.contains("todus.cu") || lowerUrl.contains("todus") || lowerUrl.contains("152.207.206.86")) {
            connection.setRequestProperty("User-Agent", "Wget/1.21.3")
            connection.setRequestProperty("Host", "s3.todus.cu")
        } else if (lowerUrl.contains("nube.uo.edu.cu") || lowerUrl.contains("nextcloud")) {
            connection.setRequestProperty("User-Agent", "NextcloudDirectClient/1.0")
            connection.setRequestProperty("OCS-APIRequest", "true")
        } else {
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            )
        }

        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Accept-Encoding", "identity")
        connection.setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
        connection.setRequestProperty("Connection", "keep-alive")

        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = permissiveSslSocketFactory
            connection.setHostnameVerifier { hostname, _ ->
                // Permite s3.todus.cu y 152.207.206.86
                true
            }
        }
        return connection
    }
}
