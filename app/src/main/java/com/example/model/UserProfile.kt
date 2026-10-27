package com.example.model

data class UserProfile(
    val isLinked: Boolean = false,
    val fullName: String = "Modo Invitado",
    val username: String = "",
    val telegramId: String = "",
    val photoUrl: String? = null,
    val accountStatus: String = "Modo Invitado",
    val balance: String = "$ 0.00 CUP",
    val quotaPlan: String = "Plan Estándar",
    val linkCode: String = "JK-7492",
    val linkedDate: String = "",
    val planName: String = "PLAN FREE",
    val planStatus: String = "Suscripción Activa",
    val planExpiration: String = "Sin caducidad (Permanente)",
    val role: String = "Usuario Estándar",
    val phone: String = "No especificado",
    val lastActivity: String = "Hace un momento",
    val trafficUsedGb: Double = 0.22,
    val trafficTotalGb: Double = 1.00,
    val cloudUsedGb: Double = 0.01,
    val cloudTotalGb: Double = 1.00,
    val cloudFilesCount: Int = 6,
    val activeDevices: Int = 1,
    val maxDevices: Int = 2,
    val uploadStrategy: String = "Directo",
    val partSizeMb: Int = 2,
    val autoUpload: Boolean = true
)

