package com.example.episense.ui.medical

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.episense.model.Report
import com.example.episense.viewmodel.MedicalDashboardViewModel
import com.example.episense.viewmodel.MedicalState
import com.example.episense.viewmodel.MedicalViewModel
import com.example.episense.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDashboardScreen(
    dashboardViewModel: MedicalDashboardViewModel = viewModel(),
    medicalViewModel: MedicalViewModel = viewModel(), // Untuk fetch list laporan
    reportViewModel: ReportViewModel = viewModel(),   // Untuk update status verifikasi
    onNavigateToMap: () -> Unit = {}
) {
    // State dari Dashboard (Statistik)
    val totalReports by dashboardViewModel.totalReports.collectAsState()
    val totalConfirmed by dashboardViewModel.totalConfirmedCases.collectAsState()
    val totalAlerts by dashboardViewModel.totalAlerts.collectAsState()
    val highRiskArea by dashboardViewModel.highRiskArea.collectAsState()
    val outbreakStatus by dashboardViewModel.outbreakStatus.collectAsState()

    // State dari List Laporan
    val uiState by medicalViewModel.uiState.collectAsState()

    // State untuk Dialog Verifikasi
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // LazyColumn utama agar layar bisa di-scroll tanpa error Nested Scroll
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // --- BAGIAN 1: HEADER & STATISTIK ---
        item {
            Text("Dashboard Pengawasan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Pusat kendali dan verifikasi data malaria", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            // Baris 1 Statistik
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Laporan", totalReports.toString(), MaterialTheme.colorScheme.primary)
                StatCard(Modifier.weight(1f), "Kasus Terkonfirmasi", totalConfirmed.toString(), Color(0xFFE53935))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Baris 2 Statistik
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Alert", totalAlerts.toString(), Color(0xFFFFB300))
                StatCard(Modifier.weight(1f), "Status Wabah", outbreakStatus, if (outbreakStatus == "Normal") Color(0xFF43A047) else Color.Red)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Card Wilayah Risiko Tinggi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Wilayah Risiko Tinggi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(highRiskArea, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Peta
            Button(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Place, contentDescription = "Map")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lihat Peta Persebaran Malaria")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Daftar Laporan Masuk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- BAGIAN 2: DAFTAR LAPORAN ---
        when (uiState) {
            is MedicalState.Loading -> {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
            is MedicalState.Success -> {
                val reportsList = (uiState as MedicalState.Success).reports
                if (reportsList.isEmpty()) {
                    item { Text("Belum ada laporan masuk.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(reportsList) { report ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                                selectedReport = report
                                showDialog = true
                            },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${report.city}, ${report.province}", fontWeight = FontWeight.Bold)
                                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                                        Text(report.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tingkat Risiko: ${report.riskLevel}", style = MaterialTheme.typography.bodyMedium, color = if(report.riskLevel == "High") Color.Red else Color.Black)
                            }
                        }
                    }
                }
            }
            is MedicalState.Error -> {
                item { Text((uiState as MedicalState.Error).message, color = MaterialTheme.colorScheme.error) }
            }
        }
    }

    // --- BAGIAN 3: DIALOG VERIFIKASI (SRS BARU) ---
    if (showDialog && selectedReport != null) {
        var staffNote by remember { mutableStateOf(selectedReport?.staffNote ?: "") }
        var currentStatus by remember { mutableStateOf(selectedReport?.status ?: "Pending") }
        var currentVerification by remember { mutableStateOf(selectedReport?.caseVerification ?: "Pending") }

        val statusOptions = listOf("Pending", "Reviewed", "Investigating", "Confirmed", "Closed")
        val verificationOptions = listOf("Pending", "Suspected", "Confirmed", "Rejected")

        var expandedStatus by remember { mutableStateOf(false) }
        var expandedVerification by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Verifikasi Laporan") },
            text = {
                Column {
                    Text("Lokasi: ${selectedReport?.city}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown Status
                    Text("Status Penanganan:", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
                        OutlinedTextField(value = currentStatus, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) })
                        ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            statusOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { currentStatus = opt; expandedStatus = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropdown Case Verification
                    Text("Verifikasi Kasus:", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(expanded = expandedVerification, onExpandedChange = { expandedVerification = !expandedVerification }) {
                        OutlinedTextField(value = currentVerification, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVerification) })
                        ExposedDropdownMenu(expanded = expandedVerification, onDismissRequest = { expandedVerification = false }) {
                            verificationOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { currentVerification = opt; expandedVerification = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Catatan Medis
                    OutlinedTextField(
                        value = staffNote,
                        onValueChange = { staffNote = it },
                        label = { Text("Catatan untuk Pasien") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Update ke Firestore
                    reportViewModel.verifyReport(
                        reportId = selectedReport!!.reportId,
                        newStatus = currentStatus,
                        caseVerification = currentVerification,
                        staffNote = staffNote,
                        medicalStaffName = "Tim Medis",
                        onSuccess = {
                            showDialog = false
                            medicalViewModel.fetchReports() // Refresh daftar
                        }
                    )
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showDialog = false }) { Text("Batal") } }
        )
    }
}

