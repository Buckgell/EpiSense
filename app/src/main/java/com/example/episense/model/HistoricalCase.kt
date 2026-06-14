package com.example.episense.model

data class HistoricalCase(
    val caseId: String = "",
    val province: String = "",
    val city: String = "",
    val date: Long = 0L, // Timestamp dalam milliseconds
    val caseCount: Int = 0,
    val rainfall: Double = 0.0,
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val populationDensity: Int = 0
)