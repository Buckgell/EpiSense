package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Custom class agar tidak bergantung pada Google Maps
data class GeoLocation(val lat: Double, val lng: Double)

data class ReportLocation(
    val id: String,
    val coordinate: GeoLocation,
    val status: String
)

class HotspotViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _hotspots = MutableStateFlow<List<ReportLocation>>(emptyList())
    val hotspots: StateFlow<List<ReportLocation>> = _hotspots

    init {
        fetchHotspots()
    }

    private fun fetchHotspots() {
        db.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val locations = snapshot.documents.mapNotNull { doc ->
                val lat = doc.getDouble("latitude")
                val lng = doc.getDouble("longitude")
                val status = doc.getString("status") ?: "Pending"

                if (lat != null && lng != null) {
                    ReportLocation(doc.id, GeoLocation(lat, lng), status)
                } else {
                    null
                }
            }
            _hotspots.value = locations
        }
    }
}