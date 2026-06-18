package com.example.episense.ui.medical

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class MedicalNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : MedicalNavItem("med_dashboard", "Laporan", Icons.Filled.List)
    object AddEducation : MedicalNavItem("med_add_edu", "Edukasi", Icons.Filled.MenuBook)
    object AddAlert : MedicalNavItem("med_add_alert", "Kirim Alert", Icons.Filled.Warning)

    object Profile : MedicalNavItem("med_profile", "Profil", Icons.Filled.AccountCircle)
}

@Composable
fun MedicalDashboardScreen(
    viewModel: com.example.episense.viewmodel.MedicalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    reportViewModel: com.example.episense.viewmodel.ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    profileViewModel: com.example.episense.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()

    var selectedReport by remember { mutableStateOf<com.example.episense.model.Report?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard Tenaga Medis", style = MaterialTheme.typography.headlineMedium)
        Text("Daftar Laporan Gejala Masyarakat", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is com.example.episense.viewmodel.MedicalState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is com.example.episense.viewmodel.MedicalState.Success -> {
                val reports = (uiState as com.example.episense.viewmodel.MedicalState.Success).reports
                if (reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada laporan masuk.") }
                } else {
                    LazyColumn {
                        items(reports.size) { index ->
                            val report = reports[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    // Tambahan clickable untuk memunculkan dialog
                                    .clickable {
                                        selectedReport = report
                                        showDialog = true
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${report.city}, ${report.province}", fontWeight = FontWeight.Bold)
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                                            Text(report.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val gejala = mutableListOf<String>()
                                    if (report.fever) gejala.add("Demam")
                                    if (report.chills) gejala.add("Menggigil")
                                    if (report.headache) gejala.add("Sakit Kepala")
                                    if (report.nausea) gejala.add("Mual")
                                    Text("Gejala: ${if (gejala.isEmpty()) "Tidak ada" else gejala.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            is com.example.episense.viewmodel.MedicalState.Error -> {
                Text((uiState as com.example.episense.viewmodel.MedicalState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Pop-up Dialog untuk Verifikasi
    if (showDialog && selectedReport != null) {
        var staffNote by remember { mutableStateOf(selectedReport?.staffNote ?: "") }
        // Default opsi status
        var expanded by remember { mutableStateOf(false) }
        var currentStatus by remember { mutableStateOf(selectedReport?.status ?: "Pending") }
        val statusOptions = listOf("Pending", "Reviewed", "Investigating", "Confirmed", "Closed")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Verifikasi & Update Laporan") },
            text = {
                Column {
                    Text("Lokasi: ${selectedReport?.city}, ${selectedReport?.province}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown untuk memilih Status
                    Text("Status Penanganan:", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(currentStatus)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        currentStatus = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = staffNote,
                        onValueChange = { staffNote = it },
                        label = { Text("Catatan Medis (Tindak Lanjut)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val staffName = userProfile?.name ?: "Unknown Staff"

                    // Kita memanggil verifyReport dan menambahkan aksi onSuccess
                    reportViewModel.verifyReport(
                        reportId = selectedReport!!.reportId,
                        newStatus = currentStatus,
                        staffNote = staffNote,
                        medicalStaffName = staffName,
                        onSuccess = {
                            // Tutup dialog
                            showDialog = false

                            // Minta MedicalViewModel untuk mengambil ulang data terbaru.
                            // Catatan: Ganti 'fetchReports()' dengan nama fungsi
                            // yang Anda gunakan di MedicalViewModel untuk mengambil data.
                            // Biasanya bernama fetchReports(), getReports(), atau loadData().
                            viewModel.fetchReports()
                        }
                    )
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun MedicalAddEducationScreen(viewModel: com.example.episense.viewmodel.AddEducationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tulis Artikel Edukasi", style = MaterialTheme.typography.headlineMedium)
        Text("Artikel ini akan tayang di halaman masyarakat", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Judul Artikel (Misal: Cara Cegah Malaria)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Isi Artikel (Penjelasan lengkap)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Lebih tinggi agar nyaman untuk menulis artikel
            maxLines = 10
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.addEducation(title, content) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is com.example.episense.viewmodel.AddEducationState.Loading
        ) {
            if (uiState is com.example.episense.viewmodel.AddEducationState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Publikasikan Artikel")
            }
        }

        // Feedback UI dengan delay agar tidak langsung hilang
        when (uiState) {
            is com.example.episense.viewmodel.AddEducationState.Success -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text("✅ Artikel Edukasi berhasil dipublikasikan!", color = androidx.compose.ui.graphics.Color.Green, fontWeight = FontWeight.Bold)

                LaunchedEffect(Unit) {
                    title = ""
                    content = ""
                    kotlinx.coroutines.delay(3000)
                    viewModel.resetState()
                }
            }
            is com.example.episense.viewmodel.AddEducationState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text("❌ ${(uiState as com.example.episense.viewmodel.AddEducationState.Error).message}", color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}

@Composable
fun MedicalAddAlertScreen(viewModel: com.example.episense.viewmodel.AddAlertViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Kirim Peringatan Baru", style = MaterialTheme.typography.headlineMedium)
        Text("Pesan ini akan disebarkan ke seluruh masyarakat", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Judul Peringatan (Misal: Waspada DBD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Isi Pesan Edukasi / Instruksi") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendAlert(title, message) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is com.example.episense.viewmodel.AddAlertState.Loading
        ) {
            if (uiState is com.example.episense.viewmodel.AddAlertState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Kirim Peringatan Sekarang")
            }
        }

        // Feedback UI setelah tombol ditekan
        when (uiState) {
            is com.example.episense.viewmodel.AddAlertState.Success -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text("✅ Peringatan berhasil dikirim ke database!", color = androidx.compose.ui.graphics.Color.Green, fontWeight = FontWeight.Bold)

                // Reset form otomatis
                LaunchedEffect(Unit) {
                    title = ""
                    message = ""
                    delay(3000)
                    viewModel.resetState()
                }
            }
            is com.example.episense.viewmodel.AddAlertState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text("❌ ${(uiState as com.example.episense.viewmodel.AddAlertState.Error).message}", color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}