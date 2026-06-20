package com.example.episense.model

data class Report(
    val reportId: String = "",
    val userId: String = "",
    val province: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: Long = System.currentTimeMillis(),

    // Gejala
    val fever: Boolean = false,
    val chills: Boolean = false,
    val headache: Boolean = false,
    val nausea: Boolean = false,
    val riskLevel: String = "Low", // Low, Medium, High

    // Status Penanganan (SRS Baru)
    val status: String = "Pending", // Pending, Reviewed, Investigating, Confirmed, Closed
    val staffNote: String = "",
    val caseVerification: String = "Pending", // Suspected, Confirmed, Rejected, Pending
    val updatedBy: String = "",
    val updatedAt: Long = 0L
)