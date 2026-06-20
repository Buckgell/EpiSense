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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
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

    // --- State dari AnalyticsViewModel (Untuk Line Chart, Bar Chart, Lag & AI) ---
    val chartData by analyticsViewModel.chartData.collectAsState()
    val smoothedData by analyticsViewModel.smoothedChartData.collectAsState()
    val seasonalityData by analyticsViewModel.seasonalityData.collectAsState()
    val lagCorrelations by analyticsViewModel.lagCorrelations.collectAsState()
    val peakMonth by analyticsViewModel.peakMonth.collectAsState()
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
        Text("Visualisasi dan prediksi data sebaran malaria", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 1. BAGIAN PIE CHART (Distribusi Status)
        // ==========================================
        SectionTitle("Distribusi Status Laporan")
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = false
                        setUsePercentValues(true)
                    }
                },
                update = { chart ->
                    val entries = statusCounts.map { PieEntry(it.value.toFloat(), it.key) }
                    chart.data = PieData(PieDataSet(entries, "").apply { colors = ColorTemplate.MATERIAL_COLORS.toList() })
                    chart.invalidate()
                }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 2. BAGIAN LINE CHART (Tren Waktu & Noise Filtering)
        // ==========================================
        SectionTitle("Tren Kasus Malaria (Time Series)")
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            if (chartData.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                        }
                    },
                    update = { chart ->
                        val dataSetRaw = LineDataSet(chartData, "Data Mentah").apply {
                            color = AndroidColor.argb(100, 255, 0, 0)
                            valueTextColor = AndroidColor.TRANSPARENT
                            lineWidth = 1.5f
                            setDrawFilled(false)
                        }
                        val dataSetSmoothed = LineDataSet(smoothedData, "Moving Avg").apply {
                            color = AndroidColor.BLUE
                            lineWidth = 3f
                            setDrawFilled(true)
                            fillColor = AndroidColor.argb(50, 0, 0, 255)
                        }
                        chart.data = LineData(dataSetRaw, dataSetSmoothed)
                        chart.invalidate()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 3. BAGIAN BAR CHART (Seasonality Analysis)
        // ==========================================
        SectionTitle("Pola Musiman (Seasonality)")
        Text("Puncak kasus rata-rata terjadi pada Bulan ke-$peakMonth", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            if (seasonalityData.isNotEmpty()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                        }
                    },
                    update = { chart ->
                        val dataSet = BarDataSet(seasonalityData, "Kasus per Bulan").apply {
                            color = AndroidColor.rgb(255, 152, 0)
                        }
                        chart.data = BarData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 4. BAGIAN LAG CORRELATION
        // ==========================================
        SectionTitle("Korelasi Curah Hujan (Lag Analysis)")
        val bestLag = lagCorrelations.maxByOrNull { it.second }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dampak curah hujan terhadap lonjakan kasus:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                lagCorrelations.forEach { (lag, corr) ->
                    Text("Lag $lag Bulan: Skor Korelasi = ${String.format("%.2f", corr)}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Korelasi Tertinggi: Lag ${bestLag?.first ?: 0} (Waktu tunggu lonjakan kasus setelah puncak hujan)", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // 5. BAGIAN AI ANALYSIS (Gemini)
        // ==========================================
        SectionTitle("AI Outbreak Analysis")
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
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
}