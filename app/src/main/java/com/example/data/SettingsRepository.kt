package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jk_downloader_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            themeMode = prefs.getString("theme_mode", "Oscuro") ?: "Oscuro",
            accentColorIndex = prefs.getInt("accent_index", 0), // Default 0 = Radiant Neon Red
            maxConcurrentDownloads = prefs.getInt("max_concurrent_downloads", 1),
            maxThreads = prefs.getInt("max_threads", 4),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            maxReconnectAttempts = prefs.getInt("max_reconnect_attempts", 5),
            reconnectDelaySeconds = prefs.getInt("reconnect_delay_seconds", 3),
            speedLimit = prefs.getString("speed_limit", "Sin Límite") ?: "Sin Límite",
            downloadSubfolder = prefs.getString("download_subfolder", "JKDOWN") ?: "JKDOWN",
            proxyEnabled = prefs.getBoolean("proxy_enabled", false),
            proxyHost = prefs.getString("proxy_host", "") ?: "",
            proxyPort = prefs.getInt("proxy_port", 8080),
            bufferSizeBytes = prefs.getInt("buffer_size", 262144),
            showNotifications = prefs.getBoolean("show_notifications", true)
        )
    }

    private fun loadUserProfile(): UserProfile {
        val isLinked = prefs.getBoolean("user_is_linked", false)
        return if (isLinked) {
            UserProfile(
                isLinked = true,
                name = prefs.getString("user_name", "JAKUDOSHY 👑 Ofc") ?: "JAKUDOSHY 👑 Ofc",
                username = prefs.getString("user_username", "@Jakudoshy") ?: "@Jakudoshy",
                linkCode = prefs.getString("user_link_code", "JK-7492") ?: "JK-7492"
            )
        } else {
            UserProfile(
                isLinked = false,
                name = "Invitado",
                username = "@invitado",
                linkCode = prefs.getString("user_link_code", "JK-7492") ?: "JK-7492"
            )
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("theme_mode", newSettings.themeMode)
            putInt("accent_index", newSettings.accentColorIndex)
            putInt("max_concurrent_downloads", newSettings.maxConcurrentDownloads)
            putInt("max_threads", newSettings.maxThreads)
            putBoolean("auto_reconnect", newSettings.autoReconnect)
            putInt("max_reconnect_attempts", newSettings.maxReconnectAttempts)
            putInt("reconnect_delay_seconds", newSettings.reconnectDelaySeconds)
            putString("speed_limit", newSettings.speedLimit)
            putString("download_subfolder", newSettings.downloadSubfolder)
            putBoolean("proxy_enabled", newSettings.proxyEnabled)
            putString("proxy_host", newSettings.proxyHost)
            putInt("proxy_port", newSettings.proxyPort)
            putInt("buffer_size", newSettings.bufferSizeBytes)
            putBoolean("show_notifications", newSettings.showNotifications)
            apply()
        }
        _settings.value = newSettings
    }

    fun updateProfile(profile: UserProfile) {
        prefs.edit().apply {
            putBoolean("user_is_linked", profile.isLinked)
            putString("user_name", profile.name)
            putString("user_username", profile.username)
            putString("user_link_code", profile.linkCode)
            apply()
        }
        _userProfile.value = profile
    }

    fun unlinkTelegram() {
        val guest = UserProfile(
            isLinked = false,
            name = "Invitado",
            username = "@invitado",
            linkCode = "JK-${(1000..9999).random()}"
        )
        updateProfile(guest)
    }

    fun linkTelegram(name: String, username: String) {
        val linked = UserProfile(
            isLinked = true,
            name = name.ifBlank { "JAKUDOSHY 👑 Ofc" },
            username = if (username.startsWith("@")) username else "@$username",
            linkCode = "JK-SYNCED"
        )
        updateProfile(linked)
    }
}
