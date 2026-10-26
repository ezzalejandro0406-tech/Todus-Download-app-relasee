package com.example.model

data class AppSettings(
    val maxThreads: Int = 16,
    val bufferSizeBytes: Int = 262144, // 256 KB
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = 10,
    val reconnectDelaySeconds: Int = 3,
    val sslSniBypass: Boolean = true,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int = 8080,
    val proxyUser: String = "",
    val proxyPass: String = "",
    val downloadSubfolder: String = "toDus Download",
    val notifyOnFinish: Boolean = true
)
