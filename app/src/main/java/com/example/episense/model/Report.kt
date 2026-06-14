package com.example.episense.model

data class Report(
    val reportId: String = "",
    val userId: String = "",
    val province: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val fever: Boolean = false,
    val chills: Boolean = false,
    val headache: Boolean = false,
    val nausea: Boolean = false,
    val riskLevel: String = "Menunggu Analisis",
    val status: String = "Pending"
)