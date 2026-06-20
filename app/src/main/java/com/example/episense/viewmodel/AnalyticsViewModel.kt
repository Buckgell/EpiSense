package com.example.episense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sqrt

class AnalyticsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 1. Time Series & Noise Filtering (Sudah Ada)
    private val _chartData = MutableStateFlow<List<Entry>>(emptyList())
    val chartData: StateFlow<List<Entry>> = _chartData

    private val _smoothedChartData = MutableStateFlow<List<Entry>>(emptyList())
    val smoothedChartData: StateFlow<List<Entry>> = _smoothedChartData

    // 2. Seasonality (Baru)
    private val _seasonalityData = MutableStateFlow<List<BarEntry>>(emptyList())
    val seasonalityData: StateFlow<List<BarEntry>> = _seasonalityData

    private val _peakMonth = MutableStateFlow(1)
    val peakMonth: StateFlow<Int> = _peakMonth

    // 3. Lag Correlation (Baru)
    private val _lagCorrelations = MutableStateFlow<List<Pair<Int, Double>>>(emptyList())
    val lagCorrelations: StateFlow<List<Pair<Int, Double>>> = _lagCorrelations

    private val _aiAnalysis = MutableStateFlow("Menunggu data untuk dianalisis...")
    val aiAnalysis: StateFlow<String> = _aiAnalysis

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "INPUT_API_KEY_GEMINI_ANDA_DI_SINI" // <-- Masukkan API Key
    )

    init {
        fetchDataAndComputeAnalytics()
    }

    private fun fetchDataAndComputeAnalytics() {
        db.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val monthlyCounts = mutableMapOf<Float, Float>()
            for (i in 1..12) monthlyCounts[i.toFloat()] = 0f

            for (doc in snapshot.documents) {
                val month = doc.getDouble("month")?.toFloat() ?: (1..12).random().toFloat()
                monthlyCounts[month] = monthlyCounts.getOrDefault(month, 0f) + 1f
            }

            // --- TAHAP 9 & 12: TIME SERIES & NOISE FILTERING ---
            val entries = monthlyCounts.map { Entry(it.key, it.value) }.sortedBy { it.x }
            _chartData.value = entries

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

            // --- TAHAP 10: SEASONALITY ANALYSIS ---
            val barEntries = monthlyCounts.map { BarEntry(it.key, it.value) }.sortedBy { it.x }
            _seasonalityData.value = barEntries
            _peakMonth.value = monthlyCounts.maxByOrNull { it.value }?.key?.toInt() ?: 1

            // --- TAHAP 11: LAG CORRELATION ANALYSIS ---
            // Karena data curah hujan (rainfall) riil butuh API BMKG/historical_cases,
            // kita gunakan baseline pola hujan tropis Indonesia sebagai variabel X.
            val dummyRainfall = listOf(300f, 280f, 250f, 150f, 100f, 50f, 30f, 20f, 40f, 100f, 200f, 280f)
            val casesArray = (1..12).map { monthlyCounts[it.toFloat()] ?: 0f }

            val lags = mutableListOf<Pair<Int, Double>>()
            for (lag in 0..4) {
                val corr = calculatePearsonCorrelation(dummyRainfall, casesArray, lag)
                lags.add(Pair(lag, corr))
            }
            _lagCorrelations.value = lags

            // Analisis AI
            analyzeTrendWithAI(monthlyCounts, lags.maxByOrNull { it.second } ?: Pair(0, 0.0))
        }
    }

    // Fungsi Matematika untuk Pearson Correlation Coefficient
    private fun calculatePearsonCorrelation(x: List<Float>, y: List<Float>, lag: Int): Double {
        if (x.size <= lag || y.size <= lag) return 0.0
        val xSub = x.dropLast(lag) // Curah hujan digeser mundur
        val ySub = y.drop(lag)     // Kasus
        val n = xSub.size
        if (n == 0) return 0.0

        val sumX = xSub.sum()
        val sumY = ySub.sum()
        val sumX2 = xSub.sumOf { (it * it).toDouble() }
        val sumY2 = ySub.sumOf { (it * it).toDouble() }
        val sumXY = xSub.zip(ySub).sumOf { (it.first * it.second).toDouble() }

        val numerator = n * sumXY - sumX * sumY
        val denominator = sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    private fun analyzeTrendWithAI(monthlyCounts: Map<Float, Float>, bestLag: Pair<Int, Double>) {
        viewModelScope.launch {
            try {
                _aiAnalysis.value = "AI sedang menyusun ringkasan epidemiologi..."
                val prompt = "Data kasus bulanan malaria: ${monthlyCounts.values}. " +
                        "Ditemukan korelasi tertinggi curah hujan dengan kasus pada Lag ${bestLag.first} bulan (skor ${String.format("%.2f", bestLag.second)}). " +
                        "Buat 2 paragraf penjelasan singkat mengenai pola musiman dan rekomendasi pencegahannya."
                val response = generativeModel.generateContent(prompt)
                _aiAnalysis.value = response.text ?: "Gagal menyusun analisis AI."
            } catch (e: Exception) {
                _aiAnalysis.value = "Gagal memuat AI: ${e.localizedMessage}"
            }
        }
    }
}