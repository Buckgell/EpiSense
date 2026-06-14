package com.example.episense.model

data class Alert(
    val alertId: String = "",
    val title: String = "",
    val message: String = "",
    val date: Long = System.currentTimeMillis(),
    val severity: String = "Medium" // Tingkat bahaya: High, Medium, Low
)