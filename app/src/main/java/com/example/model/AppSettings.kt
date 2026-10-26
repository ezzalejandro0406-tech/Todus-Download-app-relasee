package com.example.model

data class AppSettings(
    val maxThreads: Int = 16,
    val maxConcurrentDownloads: Int = 2,
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = 10,
    val reconnectDelaySeconds: Int = 3,
    val bufferSizeBytes: Int = 262144, // 256 KB
    val downloadSubfolder: String = "toDus Download",
    val showNotifications: Boolean = true
)
