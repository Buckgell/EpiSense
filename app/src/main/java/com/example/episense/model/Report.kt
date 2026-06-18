package com.example.episense.model

data class Report(
    val reportId: String = "",
    val userId: String = "",
    val province: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: Long = System.currentTimeMillis(),

    // Gejala yang dilaporkan (Tidak boleh diubah oleh medis)
    val fever: Boolean = false,
    val chills: Boolean = false,
    val headache: Boolean = false,
    val nausea: Boolean = false,
    val riskLevel: String = "Low",

    // Status Penanganan: Pending, Reviewed, Investigating, Confirmed, Closed
    val status: String = "Pending",

    // --- FIELD BARU UNTUK MEDICAL STAFF ---
    val staffNote: String = "",

    // Verifikasi Kasus: Pending, Suspected, Confirmed, Rejected
    val caseVerification: String = "Pending",

    val updatedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)