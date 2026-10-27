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
        val savedCode = prefs.getString("user_link_code", null) ?: run {
            val generated = "JK-${(100000..999999).random()}"
            prefs.edit().putString("user_link_code", generated).apply()
            generated
        }

        return if (isLinked) {
            UserProfile(
                isLinked = true,
                fullName = prefs.getString("user_full_name", "ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ") ?: "ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                username = prefs.getString("user_username", "@jakudoshy") ?: "@jakudoshy",
                telegramId = prefs.getString("user_telegram_id", "7935796405") ?: "7935796405",
                photoUrl = prefs.getString("user_photo_url", null),
                accountStatus = prefs.getString("user_account_status", "Suscripción Activa") ?: "Suscripción Activa",
                balance = prefs.getString("user_balance", "$ 150.00 CUP") ?: "$ 150.00 CUP",
                quotaPlan = prefs.getString("user_quota_plan", "Plan Free") ?: "Plan Free",
                linkCode = savedCode,
                linkedDate = prefs.getString("user_linked_date", "26 Oct 2026") ?: "26 Oct 2026",
                planName = prefs.getString("user_plan_name", "PLAN FREE") ?: "PLAN FREE",
                planStatus = prefs.getString("user_plan_status", "Suscripción Activa") ?: "Suscripción Activa",
                planExpiration = prefs.getString("user_plan_exp", "Sin caducidad (Permanente)") ?: "Sin caducidad (Permanente)",
                role = prefs.getString("user_role", "Usuario Estándar") ?: "Usuario Estándar",
                phone = prefs.getString("user_phone", "No especificado") ?: "No especificado",
                lastActivity = prefs.getString("user_last_act", "Hace un momento") ?: "Hace un momento",
                trafficUsedGb = prefs.getFloat("user_traffic_used", 0.22f).toDouble(),
                trafficTotalGb = prefs.getFloat("user_traffic_total", 1.00f).toDouble(),
                cloudUsedGb = prefs.getFloat("user_cloud_used", 0.01f).toDouble(),
                cloudTotalGb = prefs.getFloat("user_cloud_total", 1.00f).toDouble(),
                cloudFilesCount = prefs.getInt("user_cloud_files", 6),
                activeDevices = prefs.getInt("user_active_devices", 1),
                maxDevices = prefs.getInt("user_max_devices", 2),
                uploadStrategy = prefs.getString("user_upload_strat", "Directo") ?: "Directo",
                partSizeMb = prefs.getInt("user_part_size", 2),
                autoUpload = prefs.getBoolean("user_auto_upload", true)
            )
        } else {
            UserProfile(
                isLinked = false,
                fullName = "Modo Invitado",
                username = "",
                telegramId = "",
                photoUrl = null,
                accountStatus = "Modo Invitado",
                balance = "$ 0.00 CUP",
                quotaPlan = "Plan Estándar",
                linkCode = savedCode,
                linkedDate = "",
                planName = "PLAN FREE",
                planStatus = "No Vinculado",
                planExpiration = "Sin caducidad",
                role = "Invitado",
                phone = "No especificado",
                lastActivity = "Hace un momento",
                trafficUsedGb = 0.0,
                trafficTotalGb = 1.0,
                cloudUsedGb = 0.0,
                cloudTotalGb = 1.0,
                cloudFilesCount = 0,
                activeDevices = 0,
                maxDevices = 2,
                uploadStrategy = "Directo",
                partSizeMb = 2,
                autoUpload = true
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
            putString("user_full_name", profile.fullName)
            putString("user_username", profile.username)
            putString("user_telegram_id", profile.telegramId)
            putString("user_photo_url", profile.photoUrl)
            putString("user_account_status", profile.accountStatus)
            putString("user_balance", profile.balance)
            putString("user_quota_plan", profile.quotaPlan)
            putString("user_link_code", profile.linkCode)
            putString("user_linked_date", profile.linkedDate)
            putString("user_plan_name", profile.planName)
            putString("user_plan_status", profile.planStatus)
            putString("user_plan_exp", profile.planExpiration)
            putString("user_role", profile.role)
            putString("user_phone", profile.phone)
            putString("user_last_act", profile.lastActivity)
            putFloat("user_traffic_used", profile.trafficUsedGb.toFloat())
            putFloat("user_traffic_total", profile.trafficTotalGb.toFloat())
            putFloat("user_cloud_used", profile.cloudUsedGb.toFloat())
            putFloat("user_cloud_total", profile.cloudTotalGb.toFloat())
            putInt("user_cloud_files", profile.cloudFilesCount)
            putInt("user_active_devices", profile.activeDevices)
            putInt("user_max_devices", profile.maxDevices)
            putString("user_upload_strat", profile.uploadStrategy)
            putInt("user_part_size", profile.partSizeMb)
            putBoolean("user_auto_upload", profile.autoUpload)
            apply()
        }
        _userProfile.value = profile
    }

    fun unlinkTelegram() {
        val newCode = "JK-${(100000..999999).random()}"
        val guest = UserProfile(
            isLinked = false,
            fullName = "Modo Invitado",
            username = "",
            telegramId = "",
            photoUrl = null,
            accountStatus = "Modo Invitado",
            balance = "$ 0.00 CUP",
            quotaPlan = "Plan Estándar",
            linkCode = newCode,
            linkedDate = "",
            planName = "PLAN FREE",
            planStatus = "No Vinculado",
            planExpiration = "Sin caducidad",
            role = "Invitado",
            phone = "No especificado",
            lastActivity = "Hace un momento",
            trafficUsedGb = 0.0,
            trafficTotalGb = 1.0,
            cloudUsedGb = 0.0,
            cloudTotalGb = 1.0,
            cloudFilesCount = 0,
            activeDevices = 0,
            maxDevices = 2,
            uploadStrategy = "Directo",
            partSizeMb = 2,
            autoUpload = true
        )
        updateProfile(guest)
    }

    fun linkTelegram(
        fullName: String = "JAKUDOSHYꫂOfc",
        username: String = "@Jakudoshy",
        telegramId: String = "7935796405",
        photoUrl: String? = null,
        balance: String = "$ 150.00 CUP",
        quotaPlan: String = "PLAN FREE"
    ) {
        val curCode = _userProfile.value.linkCode
        val cleanUser = if (username.startsWith("@") || username.isBlank()) username else "@$username"
        val linked = UserProfile(
            isLinked = true,
            fullName = fullName.ifBlank { "JAKUDOSHYꫂOfc" },
            username = cleanUser.ifBlank { "@Jakudoshy" },
            telegramId = telegramId.ifBlank { "7935796405" },
            photoUrl = photoUrl,
            accountStatus = "Suscripción Activa",
            balance = balance,
            quotaPlan = quotaPlan,
            linkCode = curCode,
            linkedDate = "26 Oct 2026",
            planName = "PLAN FREE",
            planStatus = "Suscripción Activa",
            planExpiration = "Sin caducidad (Permanente)",
            role = "Usuario Estándar",
            phone = "No especificado",
            lastActivity = "Hace un momento",
            trafficUsedGb = 0.22,
            trafficTotalGb = 1.00,
            cloudUsedGb = 0.01,
            cloudTotalGb = 1.00,
            cloudFilesCount = 6,
            activeDevices = 1,
            maxDevices = 2,
            uploadStrategy = "Directo",
            partSizeMb = 2,
            autoUpload = true
        )
        updateProfile(linked)
    }
}
