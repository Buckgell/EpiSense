package com.example.episense.ui.medical

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.viewmodel.AnalyticsViewModel
import com.example.episense.viewmodel.MedicalState
import com.example.episense.viewmodel.MedicalViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun AnalyticsScreen(
    medicalViewModel: MedicalViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    // --- State dari MedicalViewModel (Untuk Pie Chart) ---
    val uiState by medicalViewModel.uiState.collectAsState()
    val reports = when (val state = uiState) {
        is MedicalState.Success -> state.reports
        else -> emptyList()
    }
    val statusCounts = reports.groupingBy { it.status }.eachCount()

    // --- State dari AnalyticsViewModel (Untuk Line Chart & AI) ---
    val chartData by analyticsViewModel.chartData.collectAsState()
    val smoothedData by analyticsViewModel.smoothedChartData.collectAsState() // TAMBAHAN: Data diperhalus
    val aiAnalysis by analyticsViewModel.aiAnalysis.collectAsState()

    // Agar layar bisa di-scroll karena kontennya panjang
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Analitik Epidemiologi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Visualisasi dan prediksi data sebaran malaria", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 1. BAGIAN PIE CHART (Distribusi Status)
        // ==========================================
        Text("Distribusi Status Laporan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (uiState is MedicalState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data laporan untuk dianalisis.")
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            isDrawHoleEnabled = false
                            setEntryLabelColor(AndroidColor.BLACK)
                            setUsePercentValues(true)
                        }
                    },
                    update = { chart ->
                        val entries = statusCounts.map { (status, count) ->
                            PieEntry(count.toFloat(), status)
                        }
                        val dataSet = PieDataSet(entries, "").apply {
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                            valueTextSize = 14f
                            valueTextColor = AndroidColor.BLACK
                            sliceSpace = 3f
                        }
                        chart.data = PieData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 2. BAGIAN LINE CHART (Tren Waktu & Noise Filter)
        // ==========================================
        Text("Tren Kasus Malaria (Time Series)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (chartData.isNotEmpty() && smoothedData.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.granularity = 1f
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            legend.isEnabled = true // Aktifkan legend untuk membedakan dua garis
                        }
                    },
                    update = { chart ->
                        // Garis 1: Data Asli (Merah, tipis)
                        val dataSetRaw = LineDataSet(chartData, "Data Mentah (Noise)").apply {
                            color = AndroidColor.argb(100, 255, 0, 0)
                            valueTextColor = AndroidColor.TRANSPARENT // Sembunyikan angka agar rapi
                            lineWidth = 1.5f
                            setCircleColor(AndroidColor.RED)
                            circleRadius = 3f
                            setDrawFilled(false)
                        }

                        // Garis 2: Data Diperhalus / Moving Average (Biru, tebal)
                        val dataSetSmoothed = LineDataSet(smoothedData, "Tren Utama (Moving Avg)").apply {
                            color = AndroidColor.BLUE
                            valueTextColor = AndroidColor.BLACK
                            lineWidth = 3f
                            setCircleColor(AndroidColor.BLUE)
                            circleRadius = 5f
                            setDrawFilled(true)
                            fillColor = AndroidColor.argb(50, 0, 0, 255) // Biru transparan
                        }

                        // Masukkan kedua garis ke dalam chart
                        chart.data = LineData(dataSetRaw, dataSetSmoothed)
                        chart.invalidate()
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Memuat tren grafik...")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 3. BAGIAN AI ANALYSIS (Gemini)
        // ==========================================
        Text(
            text = "AI Outbreak Analysis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = aiAnalysis,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Spasi bawah agar tidak tertutup BottomBar
    }
}