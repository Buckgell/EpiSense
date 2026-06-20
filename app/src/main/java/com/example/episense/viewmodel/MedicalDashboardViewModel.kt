package com.example.episense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MedicalDashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalReports = MutableStateFlow(0)
    val totalReports: StateFlow<Int> = _totalReports

    private val _totalConfirmedCases = MutableStateFlow(0)
    val totalConfirmedCases: StateFlow<Int> = _totalConfirmedCases

    private val _totalAlerts = MutableStateFlow(0)
    val totalAlerts: StateFlow<Int> = _totalAlerts

    private val _highRiskArea = MutableStateFlow("-")
    val highRiskArea: StateFlow<String> = _highRiskArea

    private val _outbreakStatus = MutableStateFlow("Normal")
    val outbreakStatus: StateFlow<String> = _outbreakStatus

    init {
        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        // 1. Listen ke collection "reports" untuk menghitung total laporan, kasus konfirmasi, dan wilayah risiko tinggi
        db.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("DashboardVM", "Error fetching reports: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _totalReports.value = snapshot.size()

                // Hitung total yang "Confirmed"
                var confirmedCount = 0
                val cityRiskCount = mutableMapOf<String, Int>() // Untuk mencari kota dengan risiko tinggi terbanyak

                for (doc in snapshot.documents) {
                    val caseVerification = doc.getString("caseVerification") ?: ""
                    if (caseVerification == "Confirmed") {
                        confirmedCount++
                    }

                    val riskLevel = doc.getString("riskLevel") ?: ""
                    val city = doc.getString("city") ?: "Tidak diketahui"

                    if (riskLevel == "High" || riskLevel == "Tinggi") {
                        cityRiskCount[city] = cityRiskCount.getOrDefault(city, 0) + 1
                    }
                }

                _totalConfirmedCases.value = confirmedCount

                // Tentukan wilayah risiko tinggi (kota dengan laporan 'High' terbanyak)
                val topHighRiskCity = cityRiskCount.maxByOrNull { it.value }?.key
                _highRiskArea.value = topHighRiskCity ?: "Aman"
            }
        }

        // 2. Listen ke collection "alerts" untuk menghitung total alert yang aktif
        db.collection("alerts").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                _totalAlerts.value = snapshot.size()
            }
        }

        // Catatan: _outbreakStatus sementara di-set "Normal",
        // nilainya akan diubah dinamis nanti di Tahap 6 (Outbreak Pattern Recognition)
    }
}