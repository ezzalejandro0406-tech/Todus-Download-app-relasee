package com.example.model

data class AppSettings(
    val themeMode: String = "Oscuro", // "Sistema", "Claro", "Oscuro"
    val accentColorIndex: Int = 0,    // 0 = Neon Radiant Red as requested
    val maxConcurrentDownloads: Int = 1,
    val maxThreads: Int = 4,
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = 5,
    val reconnectDelaySeconds: Int = 3,
    val speedLimit: String = "Sin Límite",
    val downloadSubfolder: String = "JKDOWN",
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int = 8080,
    val bufferSizeBytes: Int = 262144,
    val showNotifications: Boolean = true
)
