package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.Report
import com.example.episense.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    object Success : ReportState()
    data class Error(val message: String) : ReportState()
}

class ReportViewModel : ViewModel() {
    private val repository = ReportRepository()

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState
    private val _myReports = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.episense.model.Report>>(emptyList())
    val myReports: kotlinx.coroutines.flow.StateFlow<List<com.example.episense.model.Report>> = _myReports

    fun fetchMyReports(userId: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("reports")
            .whereEqualTo("userId", userId)
            // Opsional: .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Gagal mengambil riwayat laporan: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reportList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.example.episense.model.Report::class.java)
                    }
                    _myReports.value = reportList
                }
            }
    }
    fun submitReport(province: String, city: String, fever: Boolean, chills: Boolean, headache: Boolean, nausea: Boolean) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            // Dummy GPS location sementara (Tahap 10 nanti kita ganti dengan Google Maps SDK asli)
            val dummyLat = -7.250445
            val dummyLon = 112.768845

            val report = Report(
                province = province,
                city = city,
                latitude = dummyLat,
                longitude = dummyLon,
                fever = fever,
                chills = chills,
                headache = headache,
                nausea = nausea
            )

            val result = repository.submitReport(report)
            if (result.isSuccess) {
                _reportState.value = ReportState.Success
            } else {
                _reportState.value = ReportState.Error(result.exceptionOrNull()?.message ?: "Gagal mengirim laporan")
            }
        }
    }

    // Tambahkan fungsi ini di dalam ReportViewModel
    fun verifyReport(
        reportId: String,
        newStatus: String,
        staffNote: String,
        medicalStaffName: String,
        onSuccess: () -> Unit
    ) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Data yang akan di-update ke Firestore
        val updates = mapOf(
            "status" to newStatus,
            "staffNote" to staffNote,
            "caseVerification" to newStatus,
            "updatedBy" to medicalStaffName,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("reports").document(reportId)
            .update(updates)
            .addOnSuccessListener {
                println("Berhasil memverifikasi laporan!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("Gagal memverifikasi: ${e.message}")
            }
    }

    fun resetState() {
        _reportState.value = ReportState.Idle
    }
}