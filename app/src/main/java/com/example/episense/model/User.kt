package com.example.episense.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "citizen", // Default role
    val createdAt: Long = System.currentTimeMillis()
)