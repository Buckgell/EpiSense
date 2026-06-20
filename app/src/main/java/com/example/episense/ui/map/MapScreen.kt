package com.example.episense.ui.map

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MedicalViewModel = viewModel(),
    onNavigateBack: () -> Unit = {} // Parameter baru untuk tombol kembali
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    Configuration.getInstance().userAgentValue = context.packageName

    val reports = when (val state = uiState) {
        is MedicalState.Success -> state.reports
        else -> emptyList()
    }

    // Tambahan Scaffold untuk membuat Header Bar dengan tombol Back
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Sebaran Kasus") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState is MedicalState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (reports.isEmpty()) {
                Text("Tidak ada titik laporan untuk ditampilkan di peta.", modifier = Modifier.align(Alignment.Center))
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            // Posisi kamera default (Surabaya)
                            controller.setZoom(12.0)
                            controller.setCenter(GeoPoint(-7.250444, 112.768845))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()

                        reports.forEach { report ->
                            // Pastikan koordinat tidak 0.0 sebelum menaruh pin
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
                                mapView.overlays.add(marker)
                            }
                        }
                        mapView.invalidate()
                    }
                )
            }
        }
    }
}