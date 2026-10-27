package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val fileSize: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val speedBytesPerSec: Long = 0L,
    val threadsCount: Int = 16,
    val filePath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val mimeType: String = "",
    val errorMsg: String? = null,
    val retryCount: Int = 0,
    val reconnectAttempt: Int = 0,
    val chunksData: String = ""
) {
    val progress: Float
        get() = if (fileSize > 0) (downloadedBytes.toFloat() / fileSize.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()

    val formattedSize: String
        get() = formatBytes(fileSize)

    val formattedDownloaded: String
        get() = formatBytes(downloadedBytes)

    val formattedSpeed: String
        get() = if (speedBytesPerSec > 0) "${formatBytes(speedBytesPerSec)}/s" else "0 B/s"

    val fileExtension: String
        get() {
            val dotIndex = fileName.lastIndexOf('.')
            return if (dotIndex in 0 until fileName.length - 1) {
                fileName.substring(dotIndex + 1).lowercase(Locale.ROOT)
            } else ""
        }

    val isApk: Boolean get() = fileExtension == "apk"
    val isVideo: Boolean get() = fileExtension in listOf("mp4", "mkv", "avi", "mov", "webm", "flv")
    val isAudio: Boolean get() = fileExtension in listOf("mp3", "m4a", "ogg", "flac", "wav", "aac")
    val isImage: Boolean get() = fileExtension in listOf("jpg", "jpeg", "png", "webp", "gif", "svg")
    val isArchive: Boolean get() = fileExtension in listOf("zip", "rar", "7z", "tar", "gz")

    val etaSeconds: Long
        get() {
            if (speedBytesPerSec <= 0 || fileSize <= downloadedBytes) return 0L
            val remainingBytes = fileSize - downloadedBytes
            return remainingBytes / speedBytesPerSec
        }

    val formattedEta: String
        get() {
            val s = etaSeconds
            return when {
                s <= 0 -> "--"
                s < 60 -> "${s}s"
                s < 3600 -> "${s / 60}m ${s % 60}s"
                else -> "${s / 3600}h ${(s % 3600) / 60}m"
            }
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var b = bytes.toDouble()
            var i = 0
            while (b >= 1024.0 && i < units.size - 1) {
                b /= 1024.0
                i++
            }
            return if (i == 0) "${bytes} B" else String.format(Locale.US, "%.1f %s", b, units[i])
        }
    }
}
