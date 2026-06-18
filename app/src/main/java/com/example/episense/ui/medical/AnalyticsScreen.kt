package com.example.episense.ui.medical

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.viewmodel.MedicalState // Pastikan import ini sesuai dengan nama State Anda
import com.example.episense.viewmodel.MedicalViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun AnalyticsScreen(viewModel: MedicalViewModel = viewModel()) {
    // 1. Ambil uiState, bukan reports langsung
    val uiState by viewModel.uiState.collectAsState()

    // 2. Ekstrak reports HANYA jika state-nya Success
    val reports = when (val state = uiState) {
        is MedicalState.Success -> state.reports
        else -> emptyList()
    }

    // Memproses data: Menghitung jumlah laporan berdasarkan statusnya
    val statusCounts = reports.groupingBy { it.status }.eachCount()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Analitik Epidemiologi", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Distribusi Status Kasus Malaria", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        if (uiState is MedicalState.Loading) {
            Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Belum ada data laporan untuk dianalisis.")
            }
        } else {
            // Membungkus MPAndroidChart agar bisa dipakai di Compose
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = false
                        setEntryLabelColor(Color.BLACK)
                        setUsePercentValues(true)
                    }
                },
                update = { chart ->
                    // Mengisi data ke dalam Chart
                    val entries = statusCounts.map { (status, count) ->
                        PieEntry(count.toFloat(), status)
                    }

                    val dataSet = PieDataSet(entries, "").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 14f
                        valueTextColor = Color.BLACK
                        sliceSpace = 3f
                    }

                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}