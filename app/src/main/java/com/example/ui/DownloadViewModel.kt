package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.model.AppSettings
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val downloadDao = database.downloadDao()
    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    val activeDownloads: StateFlow<List<DownloadEntity>> = downloadDao.getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadEntity>> = downloadDao.getCompletedDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpeedBps: StateFlow<Long> = activeDownloads.map { list ->
        list.filter { it.status == DownloadStatus.DOWNLOADING }.sumOf { it.speedBytesPerSec }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _detailItem = MutableStateFlow<DownloadEntity?>(null)
    val detailItem: StateFlow<DownloadEntity?> = _detailItem.asStateFlow()

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun showDetail(item: DownloadEntity) {
        _detailItem.value = item
    }

    fun closeDetail() {
        _detailItem.value = null
    }

    fun addDownload(rawUrl: String, customThreads: Int? = null) {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val fileName = extractFileNameFromUrl(trimmed)
            val threads = customThreads ?: settings.value.maxThreads

            val entity = DownloadEntity(
                url = trimmed,
                fileName = fileName,
                fileSize = 0L,
                downloadedBytes = 0L,
                status = DownloadStatus.DOWNLOADING,
                threadsCount = threads,
                createdAt = System.currentTimeMillis()
            )

            val newId = downloadDao.insertDownload(entity)
            DownloadService.startDownload(getApplication(), newId)
        }
    }

    fun pauseDownload(id: Long) {
        DownloadService.pauseDownload(getApplication(), id)
    }

    fun resumeDownload(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadDao.updateStatusAndSpeed(id, DownloadStatus.DOWNLOADING, 0L)
            DownloadService.startDownload(getApplication(), id)
        }
    }

    fun cancelDownload(id: Long) {
        DownloadService.cancelDownload(getApplication(), id)
    }

    fun retryDownload(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = downloadDao.getDownloadByIdOnce(id)
            if (item != null) {
                downloadDao.updateStatusAndSpeed(id, DownloadStatus.DOWNLOADING, 0L)
                DownloadService.startDownload(getApplication(), id)
            }
        }
    }

    fun pauseAll() {
        viewModelScope.launch(Dispatchers.IO) {
            activeDownloads.value.forEach {
                DownloadService.pauseDownload(getApplication(), it.id)
            }
        }
    }

    fun resumeAll() {
        viewModelScope.launch(Dispatchers.IO) {
            activeDownloads.value.forEach {
                if (it.status == DownloadStatus.PAUSED || it.status == DownloadStatus.FAILED) {
                    resumeDownload(it.id)
                }
            }
        }
    }

    fun deleteDownload(id: Long, deleteFile: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = downloadDao.getDownloadByIdOnce(id)
            if (item != null && deleteFile && item.filePath.isNotEmpty()) {
                val file = File(item.filePath)
                if (file.exists()) file.delete()
                val partFile = File("${item.filePath}.part")
                if (partFile.exists()) partFile.delete()
            }
            downloadDao.deleteById(id)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadDao.clearCompleted()
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    fun openFile(context: Context, item: DownloadEntity) {
        val file = File(item.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo ya no existe en el almacenamiento", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val extension = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir con...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "No se encontró aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, item: DownloadEntity) {
        val file = File(item.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val extension = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir archivo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractFileNameFromUrl(urlStr: String): String {
        return try {
            val clean = urlStr.substringBefore("?").substringBefore("#")
            val rawName = clean.substringAfterLast("/")
            val decoded = URLDecoder.decode(rawName, "UTF-8")
            if (decoded.isNotBlank() && decoded.contains(".")) decoded else "download_${System.currentTimeMillis()}"
        } catch (_: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }
}
