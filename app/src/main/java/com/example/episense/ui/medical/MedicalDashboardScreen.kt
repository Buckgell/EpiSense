package com.example.episense.ui.medical

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.viewmodel.MedicalDashboardViewModel

@Composable
fun MedicalDashboardScreen(
    viewModel: MedicalDashboardViewModel = viewModel(),
    onNavigateToMap: () -> Unit = {}
) {
    val totalReports by viewModel.totalReports.collectAsState()
    val totalConfirmed by viewModel.totalConfirmedCases.collectAsState()
    val totalAlerts by viewModel.totalAlerts.collectAsState()
    val highRiskArea by viewModel.highRiskArea.collectAsState()
    val outbreakStatus by viewModel.outbreakStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard Pengawasan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ringkasan data malaria terkini",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grid untuk menampilkan angka statistik
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(title = "Total Laporan", value = totalReports.toString(), color = MaterialTheme.colorScheme.primary)
            }
            item {
                StatCard(title = "Kasus Terkonfirmasi", value = totalConfirmed.toString(), color = Color(0xFFE53935)) // Merah
            }
            item {
                StatCard(title = "Total Alert", value = totalAlerts.toString(), color = Color(0xFFFFB300)) // Amber
            }
            item {
                StatCard(title = "Status Wabah", value = outbreakStatus, color = if (outbreakStatus == "Normal") Color(0xFF43A047) else Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card lebar khusus untuk Wilayah Risiko Tinggi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Wilayah Risiko Tinggi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(highRiskArea, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TAMBAHAN: Tombol untuk membuka Peta Persebaran (Hotspot)
        Button(
            onClick = onNavigateToMap,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Place, contentDescription = "Map Icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lihat Peta Persebaran Malaria")
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}