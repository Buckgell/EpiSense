package com.example.episense.ui.map

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
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

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true) // Agar bisa di-zoom dengan dua jari
                val mapController = controller
                mapController.setZoom(12.0)

                // Koordinat default (misal: Surabaya)
                val startPoint = GeoPoint(-7.250445, 112.768845)
                mapController.setCenter(startPoint)
            }
        },
        update = { mapView ->
            // Bersihkan overlay (marker/lingkaran) lama agar tidak menumpuk saat data berubah
            mapView.overlays.clear()

            hotspots.forEach { report ->
                val point = GeoPoint(report.coordinate.lat, report.coordinate.lng)

                // 1. Gambar Radius Spasial (Spatial Smoothing - Tahap 14)
                val circle = Polygon()
                circle.points = Polygon.pointsAsCircle(point, 1000.0) // Radius 1 kilometer

                val fillAlpha = 80 // Transparansi (0-255)
                when (report.status) {
                    "Confirmed" -> {
                        circle.fillPaint.color = AndroidColor.argb(fillAlpha, 255, 0, 0)
                        circle.outlinePaint.color = AndroidColor.RED
                    }
                    "Investigating", "Pending" -> {
                        circle.fillPaint.color = AndroidColor.argb(fillAlpha, 255, 165, 0)
                        circle.outlinePaint.color = AndroidColor.rgb(255, 165, 0) // Orange
                    }
                    else -> {
                        circle.fillPaint.color = AndroidColor.argb(fillAlpha, 0, 255, 0)
                        circle.outlinePaint.color = AndroidColor.GREEN
                    }
                }
                circle.outlinePaint.strokeWidth = 3f
                mapView.overlays.add(circle)

                // 2. Pasang Pin Marker (Titik Tengah)
                val marker = Marker(mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "ID: ${report.id}"
                marker.snippet = "Status Kasus: ${report.status}"
                mapView.overlays.add(marker)
            }

            // Refresh Peta setelah menambahkan layer
            mapView.invalidate()
        }
    )
}