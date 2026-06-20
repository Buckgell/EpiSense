package com.example.episense.ui.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.episense.viewmodel.AlertState
import com.example.episense.viewmodel.AlertViewModel

fun formatAlertDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@Composable
fun CitizenAlertScreen(
    viewModel: AlertViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Peringatan Kesehatan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.fetchAlerts() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is AlertState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AlertState.Error -> {
                val errorMessage = (uiState as AlertState.Error).message
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Terjadi Kesalahan:\n$errorMessage", color = MaterialTheme.colorScheme.error)
                }
            }
            is AlertState.Success -> {
                val alerts = (uiState as AlertState.Success).alerts

                if (alerts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aman! Belum ada peringatan saat ini.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alerts) { alert ->
                            AlertCard(alert = alert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: com.example.episense.model.Alert) {
    val indicatorColor = when (alert.severity.lowercase()) {
        "high" -> Color.Red
        "medium" -> Color(0xFFFFA500)
        else -> Color.Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = indicatorColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = alert.message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    // Sesuaikan ke alert.date jika model Alert.kt Anda menggunakan variabel date
                    text = formatAlertDate(alert.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}