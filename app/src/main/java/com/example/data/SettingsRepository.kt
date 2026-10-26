package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("todus_download_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            maxThreads = prefs.getInt("max_threads", 16),
            bufferSizeBytes = prefs.getInt("buffer_size", 262144),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            maxReconnectAttempts = prefs.getInt("max_reconnect_attempts", 10),
            reconnectDelaySeconds = prefs.getInt("reconnect_delay_seconds", 3),
            sslSniBypass = prefs.getBoolean("ssl_sni_bypass", true),
            proxyEnabled = prefs.getBoolean("proxy_enabled", false),
            proxyHost = prefs.getString("proxy_host", "") ?: "",
            proxyPort = prefs.getInt("proxy_port", 8080),
            proxyUser = prefs.getString("proxy_user", "") ?: "",
            proxyPass = prefs.getString("proxy_pass", "") ?: "",
            downloadSubfolder = prefs.getString("download_subfolder", "toDus Download") ?: "toDus Download",
            notifyOnFinish = prefs.getBoolean("notify_on_finish", true)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putInt("max_threads", newSettings.maxThreads)
            putInt("buffer_size", newSettings.bufferSizeBytes)
            putBoolean("auto_reconnect", newSettings.autoReconnect)
            putInt("max_reconnect_attempts", newSettings.maxReconnectAttempts)
            putInt("reconnect_delay_seconds", newSettings.reconnectDelaySeconds)
            putBoolean("ssl_sni_bypass", newSettings.sslSniBypass)
            putBoolean("proxy_enabled", newSettings.proxyEnabled)
            putString("proxy_host", newSettings.proxyHost)
            putInt("proxy_port", newSettings.proxyPort)
            putString("proxy_user", newSettings.proxyUser)
            putString("proxy_pass", newSettings.proxyPass)
            putString("download_subfolder", newSettings.downloadSubfolder)
            putBoolean("notify_on_finish", newSettings.notifyOnFinish)
            apply()
        }
        _settings.value = newSettings
    }
}
