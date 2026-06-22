package com.example.episense.ui.map

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.viewmodel.HotspotViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HotspotViewModel = viewModel()
) {
    val context = LocalContext.current
    val hotspots by viewModel.hotspots.collectAsState()

    // Syarat wajib OSMDroid: Konfigurasi User-Agent agar tidak diblokir server peta
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Membungkus layar dengan Scaffold agar kita bisa menambahkan Bar Navigasi di atas
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Sebaran Kasus") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Padding ini mencegah peta tertutup oleh TopAppBar
            factory = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true) // Agar bisa di-zoom dengan dua jari
                    val mapController = controller
                    mapController.setZoom(12.0)

                    // Koordinat default awal peta dibuka
                    val startPoint = GeoPoint(-7.250445, 112.768845)
                    mapController.setCenter(startPoint)
                }
            },
            update = { mapView ->
                // Bersihkan overlay lama agar marker tidak menumpuk ganda
                mapView.overlays.clear()

                hotspots.forEach { report ->
                    val point = GeoPoint(report.coordinate.lat, report.coordinate.lng)

                    // 1. Gambar Radius Spasial (Lingkaran)
                    val circle = Polygon()
                    circle.points = Polygon.pointsAsCircle(point, 1000.0) // Radius 1 KM

                    val fillAlpha = 80 // Transparansi
                    when (report.status) {
                        "Confirmed" -> {
                            circle.fillPaint.color = AndroidColor.argb(fillAlpha, 255, 0, 0)
                            circle.outlinePaint.color = AndroidColor.RED
                        }
                        "Investigating", "Pending" -> {
                            circle.fillPaint.color = AndroidColor.argb(fillAlpha, 255, 165, 0)
                            circle.outlinePaint.color = AndroidColor.rgb(255, 165, 0)
                        }
                        else -> {
                            circle.fillPaint.color = AndroidColor.argb(fillAlpha, 0, 255, 0)
                            circle.outlinePaint.color = AndroidColor.GREEN
                        }
                    }
                    circle.outlinePaint.strokeWidth = 3f
                    mapView.overlays.add(circle)

                    // 2. Pasang Pin Marker (Titik Pusat)
                    val marker = Marker(mapView)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "ID: ${report.id}"
                    marker.snippet = "Status: ${report.status}"
                    mapView.overlays.add(marker)
                }

                // Refresh peta agar layer gambar yang baru termuat
                mapView.invalidate()
            }
        )
    }
}