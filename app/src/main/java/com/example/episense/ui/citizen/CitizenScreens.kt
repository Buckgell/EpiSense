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
import com.example.episense.viewmodel.ReportState
import com.example.episense.viewmodel.ReportViewModel

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("KAMU HITAM MALARIA")
    }
}

@Composable
fun EducationScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("KAMU CIBAI MALARIA")
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
fun AlertScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("KAMU CIBUL MALARIA")
    }
}

@Composable
fun AIScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("KAMU CABUL MALARIA")
    }
}