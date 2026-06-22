package com.example.episense.ui.medical

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.model.Report
import com.example.episense.viewmodel.*

sealed class MedicalNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : MedicalNavItem("med_dashboard", "Laporan", Icons.Filled.List)

    // PERBAIKAN 2: Menggunakan Icons.Filled.Info agar tidak crash
    object Analytics : MedicalNavItem("analytics_tab", "Analitik", Icons.Filled.Info)

    // PERBAIKAN 3: Menggunakan Icons.Filled.Create agar tidak crash
    object AddEducation : MedicalNavItem("med_add_edu", "Edukasi", Icons.Filled.Create)

    object AddAlert : MedicalNavItem("med_add_alert", "Kirim Alert", Icons.Filled.Warning)
    object Profile : MedicalNavItem("med_profile", "Profil", Icons.Filled.AccountCircle)
}

// ... Sisa kode fungsi MedicalDashboardScreen dkk biarkan seperti semula ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDashboardScreen(
    dashboardViewModel: MedicalDashboardViewModel = viewModel(),
    medicalViewModel: MedicalViewModel = viewModel(),
    reportViewModel: ReportViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToMap: () -> Unit = {}
) {
    val totalReports by dashboardViewModel.totalReports.collectAsState()
    val totalConfirmed by dashboardViewModel.totalConfirmedCases.collectAsState()
    val totalAlerts by dashboardViewModel.totalAlerts.collectAsState()
    val highRiskArea by dashboardViewModel.highRiskArea.collectAsState()
    val outbreakStatus by dashboardViewModel.outbreakStatus.collectAsState()

    val uiState by medicalViewModel.uiState.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()

    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- HEADER STATISTIK ---
        item {
            Text("Dashboard Pengawasan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Pusat kendali dan verifikasi data malaria", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Laporan", totalReports.toString(), MaterialTheme.colorScheme.primary)
                StatCard(Modifier.weight(1f), "Kasus Terkonfirmasi", totalConfirmed.toString(), Color(0xFFE53935))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Alert", totalAlerts.toString(), Color(0xFFFFB300))
                StatCard(Modifier.weight(1f), "Status Wabah", outbreakStatus, if (outbreakStatus == "Normal") Color(0xFF43A047) else Color.Red)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
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

            Button(onClick = onNavigateToMap, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Place, contentDescription = "Map")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lihat Peta Persebaran Malaria")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Daftar Laporan Masuk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- DAFTAR LAPORAN ---
        when (uiState) {
            is MedicalState.Loading -> item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
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
            is MedicalState.Error -> item { Text((uiState as MedicalState.Error).message, color = MaterialTheme.colorScheme.error) }
        }
    }

    // --- DIALOG VERIFIKASI ---
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

                    Text("Status Penanganan:", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
                        OutlinedTextField(value = currentStatus, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) })
                        ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            statusOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { currentStatus = opt; expandedStatus = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // PERBAIKAN: Input untuk Case Verification agar cocok dengan ViewModel
                    Text("Verifikasi Kasus:", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(expanded = expandedVerification, onExpandedChange = { expandedVerification = !expandedVerification }) {
                        OutlinedTextField(value = currentVerification, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVerification) })
                        ExposedDropdownMenu(expanded = expandedVerification, onDismissRequest = { expandedVerification = false }) {
                            verificationOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { currentVerification = opt; expandedVerification = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = staffNote, onValueChange = { staffNote = it }, label = { Text("Catatan untuk Pasien") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val staffName = userProfile?.name ?: "Tim Medis"

                    // PERBAIKAN: Memanggil verifyReport dengan parameter lengkap
                    reportViewModel.verifyReport(
                        reportId = selectedReport!!.reportId,
                        newStatus = currentStatus,
                        caseVerification = currentVerification, // Parameter yang hilang tadi
                        staffNote = staffNote,
                        medicalStaffName = staffName,
                        onSuccess = {
                            showDialog = false
                            medicalViewModel.fetchReports()
                        }
                    )
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(modifier = modifier.height(100.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalAddEducationScreen(viewModel: AddEducationViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Publikasi Edukasi", style = MaterialTheme.typography.headlineMedium)
        Text("Berikan panduan dan informasi kesehatan kepada masyarakat.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Artikel") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Isi Panduan") }, modifier = Modifier.fillMaxWidth(), minLines = 6, maxLines = 12)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.addEducation(title, content) },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && content.isNotBlank() && uiState !is AddEducationState.Loading
        ) {
            if (uiState is AddEducationState.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Publikasikan Edukasi")
        }

        when (uiState) {
            is AddEducationState.Success -> {
                Text("Artikel edukasi berhasil dipublikasikan!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
                LaunchedEffect(Unit) { title = ""; content = ""; viewModel.resetState() }
            }
            is AddEducationState.Error -> Text((uiState as AddEducationState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalAddAlertScreen(viewModel: AddAlertViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Medium") }
    var expanded by remember { mutableStateOf(false) }
    val severities = listOf("Low", "Medium", "High")
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buat Peringatan Baru", style = MaterialTheme.typography.headlineMedium)
        Text("Kirim notifikasi waspada wabah ke seluruh warga.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Peringatan") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Detail Pesan") }, modifier = Modifier.fillMaxWidth(), maxLines = 5)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = severity, onValueChange = {}, readOnly = true, label = { Text("Tingkat Bahaya") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                severities.forEach { selection -> DropdownMenuItem(text = { Text(selection) }, onClick = { severity = selection; expanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.addAlert(title, message, severity) },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && message.isNotBlank() && uiState !is AddAlertState.Loading
        ) {
            if (uiState is AddAlertState.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Kirim Peringatan")
        }

        when (uiState) {
            is AddAlertState.Success -> {
                Text("Peringatan berhasil dikirim!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
                LaunchedEffect(Unit) { title = ""; message = ""; severity = "Medium" }
            }
            is AddAlertState.Error -> Text((uiState as AddAlertState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            else -> {}
        }
    }
}