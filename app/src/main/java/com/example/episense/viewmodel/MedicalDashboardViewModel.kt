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

    private val _outbreakStatus = MutableStateFlow("Menghitung...")
    val outbreakStatus: StateFlow<String> = _outbreakStatus

    init {
        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        db.collection("reports").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e("DashboardVM", "Error fetching reports", error)
                return@addSnapshotListener
            }

            _totalReports.value = snapshot.size()

            var confirmedCount = 0
            val cityRiskCount = mutableMapOf<String, Int>()

            // Variabel untuk algoritma Outbreak Detection
            var currentWeekCases = 0
            var previous4WeeksCases = 0
            val currentTime = System.currentTimeMillis()
            val oneWeekInMillis = 7L * 24 * 60 * 60 * 1000

            for (doc in snapshot.documents) {
                try {
                    // PERBAIKAN: Membungkus pengambilan data dengan try-catch
                    // Jika ada data lama yang tipe datanya aneh/rusak, tidak akan membuat aplikasi Force Close!
                    val caseVerification = try { doc.getString("caseVerification") ?: "" } catch (e: Exception) { "" }
                    val riskLevel = try { doc.getString("riskLevel") ?: "" } catch (e: Exception) { "" }
                    val city = try { doc.getString("city") ?: "Tidak diketahui" } catch (e: Exception) { "Tidak diketahui" }

                    // Amankan field date (karena sering beda format antara Long dan Timestamp di data lama)
                    val date = try {
                        doc.getLong("date") ?: 0L
                    } catch (e: Exception) {
                        try { doc.getTimestamp("date")?.toDate()?.time ?: 0L } catch (e2: Exception) { 0L }
                    }

                    if (riskLevel == "High" || riskLevel == "Tinggi") {
                        cityRiskCount[city] = cityRiskCount.getOrDefault(city, 0) + 1
                    }

                    if (caseVerification == "Confirmed") {
                        confirmedCount++

                        // Hitung kasus berdasarkan waktu untuk Outbreak Detection
                        val timeDiff = currentTime - date
                        if (timeDiff <= oneWeekInMillis) {
                            currentWeekCases++
                        } else if (timeDiff <= 5 * oneWeekInMillis) {
                            previous4WeeksCases++
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DashboardVM", "Melewati dokumen yang rusak: ${doc.id}", e)
                }
            }

            _totalConfirmedCases.value = confirmedCount
            _highRiskArea.value = cityRiskCount.maxByOrNull { it.value }?.key ?: "Aman"

            // Logika Outbreak Recognition SRS
            val avgPrevious4Weeks = previous4WeeksCases / 4.0
            if (avgPrevious4Weeks > 0 && currentWeekCases > (1.5 * avgPrevious4Weeks)) {
                _outbreakStatus.value = "⚠️ WABAH TERDETEKSI"
            } else {
                _outbreakStatus.value = "Normal"
            }
        }

        db.collection("alerts").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) _totalAlerts.value = snapshot.size()
        }
    }
}