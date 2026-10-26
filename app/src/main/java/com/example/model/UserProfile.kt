package com.example.model

data class UserProfile(
    val isLinked: Boolean = false,
    val name: String = "Invitado",
    val username: String = "@invitado",
    val photoUrl: String? = null,
    val linkCode: String = "JK-7492"
)
