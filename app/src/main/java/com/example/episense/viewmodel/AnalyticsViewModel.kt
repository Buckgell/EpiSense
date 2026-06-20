package com.example.episense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class AnalyticsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Data mentah (Dengan Noise)
    private val _chartData = MutableStateFlow<List<Entry>>(emptyList())
    val chartData: StateFlow<List<Entry>> = _chartData

    // Data hasil Noise Filtering (Moving Average)
    private val _smoothedChartData = MutableStateFlow<List<Entry>>(emptyList())
    val smoothedChartData: StateFlow<List<Entry>> = _smoothedChartData

    private val _aiAnalysis = MutableStateFlow("Menunggu data untuk dianalisis...")
    val aiAnalysis: StateFlow<String> = _aiAnalysis

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "INPUT_API_KEY_GEMINI_ANDA_DI_SINI" // <-- PASTIKAN ISI API KEY LAGI
    )

    init {
        fetchTimeSeriesData()
    }

    private fun fetchTimeSeriesData() {
        db.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val monthlyCounts = mutableMapOf<Float, Float>()
            for (i in 1..12) monthlyCounts[i.toFloat()] = 0f

            for (doc in snapshot.documents) {
                // Di aplikasi nyata bisa diekstrak dari Date(timestamp).month
                // Untuk simulasi, kita asumsi tersebar acak atau ambil field month
                val month = doc.getDouble("month")?.toFloat() ?: (1..12).random().toFloat()
                monthlyCounts[month] = monthlyCounts.getOrDefault(month, 0f) + 1f
            }

            // 1. Data Asli
            val entries = monthlyCounts.map { Entry(it.key, it.value) }.sortedBy { it.x }
            _chartData.value = entries

            // 2. Data Smoothed (Noise Filtering dengan Moving Average Window=3)
            val smoothedEntries = mutableListOf<Entry>()
            val windowSize = 3
            for (i in entries.indices) {
                var sum = 0f
                var count = 0
                for (j in max(0, i - windowSize + 1)..i) {
                    sum += entries[j].y
                    count++
                }
                smoothedEntries.add(Entry(entries[i].x, sum / count))
            }
            _smoothedChartData.value = smoothedEntries

            analyzeTrendWithAI(monthlyCounts)
        }
    }

    private fun analyzeTrendWithAI(monthlyCounts: Map<Float, Float>) {
        viewModelScope.launch {
            try {
                _aiAnalysis.value = "AI sedang menganalisis tren noise filtering..."
                val dataString = monthlyCounts.entries.joinToString(", ") { "Bulan ${it.key.toInt()}: ${it.value.toInt()}" }
                val prompt = "Kamu adalah analis data kesehatan. Berikut adalah data kasus malaria bulanan: $dataString. Sistem telah menerapkan Noise Filtering (Moving Average). Berikan 2 paragraf singkat analisis tren ini dan prediksi potensi wabah ke depan."
                val response = generativeModel.generateContent(prompt)
                _aiAnalysis.value = response.text ?: "Analisis selesai, namun tidak ada insight."
            } catch (e: Exception) {
                _aiAnalysis.value = "Gagal memuat analisis AI: ${e.localizedMessage}"
            }
        }
    }
}