package com.example.model

data class UserProfile(
    val isLinked: Boolean = false,
    val fullName: String = "Modo Invitado",
    val telegramId: String = "No Vinculado",
    val accountStatus: String = "Cuenta Invitado",
    val balance: String = "$ 0.00 CUP",
    val quotaPlan: String = "Plan Estándar",
    val linkCode: String = "JK-7492",
    val linkedDate: String = ""
)
