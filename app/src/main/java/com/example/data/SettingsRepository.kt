package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("todus_download_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            maxThreads = prefs.getInt("default_threads", 16),
            maxConcurrentDownloads = prefs.getInt("max_concurrent_downloads", 2),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            maxReconnectAttempts = prefs.getInt("max_reconnect_attempts", 10),
            reconnectDelaySeconds = prefs.getInt("reconnect_delay_seconds", 3),
            bufferSizeBytes = prefs.getInt("buffer_size", 262144),
            downloadSubfolder = prefs.getString("download_dir", "toDus Download") ?: "toDus Download",
            showNotifications = prefs.getBoolean("show_notifications", true)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putInt("default_threads", newSettings.maxThreads)
            putInt("max_concurrent_downloads", newSettings.maxConcurrentDownloads)
            putBoolean("auto_reconnect", newSettings.autoReconnect)
            putInt("max_reconnect_attempts", newSettings.maxReconnectAttempts)
            putInt("reconnect_delay_seconds", newSettings.reconnectDelaySeconds)
            putInt("buffer_size", newSettings.bufferSizeBytes)
            putString("download_dir", newSettings.downloadSubfolder)
            putBoolean("show_notifications", newSettings.showNotifications)
            apply()
        }
        _settings.value = newSettings
    }
}
