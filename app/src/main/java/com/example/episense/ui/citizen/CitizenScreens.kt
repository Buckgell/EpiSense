package com.example.episense.ui.citizen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.episense.viewmodel.ReportState
import com.example.episense.viewmodel.ReportViewModel

@Composable
fun HomeScreen(viewModel: com.example.episense.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Halo, Warga!", style = MaterialTheme.typography.headlineMedium)
        Text("Berikut adalah ringkasan situasi malaria saat ini.", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is com.example.episense.viewmodel.HomeState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is com.example.episense.viewmodel.HomeState.Success -> {
                val state = uiState as com.example.episense.viewmodel.HomeState.Success

                // 1. Kartu Peringatan Terbaru
                state.latestAlert?.let { alert ->
                    Text("Peringatan Terbaru", style = MaterialTheme.typography.titleMedium)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.severity == "High") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                            Text(text = alert.message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. Kartu Statistik Kasus
                Text("Statistik 30 Laporan Terakhir", style = MaterialTheme.typography.titleMedium)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Kasus Terdeteksi", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "${state.totalRecentCases}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Di wilayah pantauan sistem", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                    }
                }
            }
            is com.example.episense.viewmodel.HomeState.Error -> {
                Text(
                    text = (uiState as com.example.episense.viewmodel.HomeState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EducationScreen(viewModel: com.example.episense.viewmodel.EducationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Edukasi Malaria", style = MaterialTheme.typography.headlineMedium)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is com.example.episense.viewmodel.EducationState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is com.example.episense.viewmodel.EducationState.Success -> {
                val educations = (uiState as com.example.episense.viewmodel.EducationState.Success).educations
                androidx.compose.foundation.lazy.LazyColumn {
                    items(educations.size) { index ->
                        val edu = educations[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = edu.title, style = MaterialTheme.typography.titleLarge)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                Text(text = edu.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            is com.example.episense.viewmodel.EducationState.Error -> {
                Text(
                    text = (uiState as com.example.episense.viewmodel.EducationState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ReportScreen(viewModel: ReportViewModel = viewModel()) {
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var fever by remember { mutableStateOf(false) }
    var chills by remember { mutableStateOf(false) }
    var headache by remember { mutableStateOf(false) }
    var nausea by remember { mutableStateOf(false) }

    val reportState by viewModel.reportState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(reportState) {
        when (reportState) {
            is ReportState.Success -> {
                Toast.makeText(context, "Laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                // Reset form
                province = ""
                city = ""
                fever = false
                chills = false
                headache = false
                nausea = false
                viewModel.resetState()
            }
            is ReportState.Error -> {
                val msg = (reportState as ReportState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lapor Gejala Malaria", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Provinsi") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Kota") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text("Gejala yang dialami:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = fever, onCheckedChange = { fever = it })
            Text("Demam")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = chills, onCheckedChange = { chills = it })
            Text("Menggigil")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = headache, onCheckedChange = { headache = it })
            Text("Sakit Kepala")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = nausea, onCheckedChange = { nausea = it })
            Text("Mual")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.submitReport(province, city, fever, chills, headache, nausea) },
            modifier = Modifier.fillMaxWidth(),
            enabled = province.isNotBlank() && city.isNotBlank()
        ) {
            Text(if (reportState == ReportState.Loading) "Mengirim..." else "Kirim Laporan")
        }
    }
}

@Composable
fun AlertScreen(viewModel: com.example.episense.viewmodel.AlertViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Peringatan Darurat", style = MaterialTheme.typography.headlineMedium)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is com.example.episense.viewmodel.AlertState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is com.example.episense.viewmodel.AlertState.Success -> {
                val alerts = (uiState as com.example.episense.viewmodel.AlertState.Success).alerts
                if (alerts.isEmpty()) {
                    Text("Tidak ada peringatan saat ini.")
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(alerts.size) { index ->
                            val alert = alerts[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                // Jika severity High, warna background akan sedikit merah
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alert.severity == "High") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = alert.title, style = MaterialTheme.typography.titleLarge)
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = alert.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            is com.example.episense.viewmodel.AlertState.Error -> {
                Text(
                    text = (uiState as com.example.episense.viewmodel.AlertState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AIScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("KAMU CABUL MALARIA")
    }
}