package com.example.episense.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.viewmodel.MedicalState
import com.example.episense.viewmodel.MedicalViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(viewModel: MedicalViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Wajib untuk OSMDroid: Mengatur User Agent agar peta diizinkan untuk diunduh
    Configuration.getInstance().userAgentValue = context.packageName

    // Ambil data laporan
    val reports = when (val state = uiState) {
        is MedicalState.Success -> state.reports
        else -> emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState is MedicalState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (reports.isEmpty()) {
            Text("Tidak ada titik laporan untuk ditampilkan di peta.", modifier = Modifier.align(Alignment.Center))
        } else {
            // Membungkus MapView klasik agar jalan di Jetpack Compose
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        // Menggunakan gaya peta standar OpenStreetMap
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true) // Bisa di-zoom pakai dua jari

                        // Set titik awal kamera ke area Surabaya Raya
                        controller.setZoom(12.0)
                        controller.setCenter(GeoPoint(-7.250444, 112.768845))
                    }
                },
                update = { mapView ->
                    // Bersihkan marker lama sebelum memasukkan yang baru
                    mapView.overlays.clear()

                    reports.forEach { report ->
                        if (report.latitude != 0.0 && report.longitude != 0.0) {
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(report.latitude, report.longitude)
                            marker.title = report.city

                            val gejala = mutableListOf<String>()
                            if (report.fever) gejala.add("Demam")
                            if (report.chills) gejala.add("Menggigil")
                            if (report.headache) gejala.add("Sakit Kepala")
                            if (report.nausea) gejala.add("Mual")

                            marker.snippet = "Status: ${report.status} | Gejala: ${if(gejala.isEmpty()) "Tidak ada" else gejala.joinToString(", ")}"

                            // Tambahkan marker ke peta
                            mapView.overlays.add(marker)
                        }
                    }
                    mapView.invalidate() // Refresh tampilan peta
                }
            )
        }
    }
}