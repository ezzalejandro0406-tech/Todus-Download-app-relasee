package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.engine.DownloaderEngine
import com.example.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var engine: DownloaderEngine
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_DOWNLOADS = "todus_downloads_channel"
        const val CHANNEL_DONE = "todus_done_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "cu.todus.download.START"
        const val ACTION_PAUSE = "cu.todus.download.PAUSE"
        const val ACTION_CANCEL = "cu.todus.download.CANCEL"
        const val ACTION_PAUSE_ALL = "cu.todus.download.PAUSE_ALL"
        const val ACTION_RESUME_ALL = "cu.todus.download.RESUME_ALL"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        fun startDownload(context: Context, downloadId: Long) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseDownload(context: Context, downloadId: Long) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }

        fun cancelDownload(context: Context, downloadId: Long) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        settingsRepository = SettingsRepository(this)
        engine = DownloaderEngine(this, database.downloadDao(), serviceScope)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildNotification("toDus Download", "Iniciando…", 0, 0, false))

        serviceScope.launch {
            database.downloadDao().getActiveDownloads().collectLatest { activeList ->
                val downloadingItems = activeList.filter {
                    it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.RECONNECTING
                }
                if (downloadingItems.isEmpty()) {
                    val pausedItems = activeList.filter { it.status == DownloadStatus.PAUSED }
                    if (pausedItems.isNotEmpty()) {
                        updateNotification("toDus Download", "En pausa · Toca Reanudar para continuar", 0, 0, false)
                    } else {
                        updateNotification("toDus Download", "Esperando turno", 0, 0, false)
                    }
                } else {
                    val count = downloadingItems.size
                    val totalSpeed = downloadingItems.sumOf { it.speedBytesPerSec }
                    val speedStr = if (totalSpeed > 0) "${com.example.model.DownloadEntity.formatBytes(totalSpeed)}/s" else "Calculando velocidad…"
                    val title = if (count == 1) downloadingItems.first().fileName else "Descargando $count archivos"
                    val subtitle = "$speedStr · ${downloadingItems.first().formattedDownloaded}/${downloadingItems.first().formattedSize}"
                    val progress = if (downloadingItems.first().fileSize > 0) {
                        (downloadingItems.first().downloadedBytes * 100 / downloadingItems.first().fileSize).toInt()
                    } else 0

                    updateNotification(title, subtitle, progress, 100, progress == 0)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadId = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1L) ?: -1L
        when (intent?.action) {
            ACTION_START -> {
                if (downloadId != -1L) {
                    val currentSettings = settingsRepository.settings.value
                    engine.startDownload(downloadId, currentSettings)
                }
            }
            ACTION_PAUSE -> {
                if (downloadId != -1L) {
                    engine.pauseDownload(downloadId)
                }
            }
            ACTION_CANCEL -> {
                if (downloadId != -1L) {
                    engine.cancelDownload(downloadId)
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progressChannel = NotificationChannel(
                CHANNEL_DOWNLOADS,
                "Progreso de las descargas activas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val doneChannel = NotificationChannel(
                CHANNEL_DONE,
                "Avisos cuando una descarga termina o falla",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(progressChannel)
            notificationManager.createNotificationChannel(doneChannel)
        }
    }

    private fun buildNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setProgress(maxProgress, progress, indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean
    ) {
        val notification = buildNotification(title, content, progress, maxProgress, indeterminate)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
