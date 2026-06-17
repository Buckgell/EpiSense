package com.example.episense.repository

import com.example.episense.model.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReportRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun submitReport(report: Report): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User belum login")
            val reportId = UUID.randomUUID().toString()
            val newReport = report.copy(reportId = reportId, userId = userId)

            db.collection("reports").document(reportId).set(newReport).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- TAMBAHKAN FUNGSI INI ---
    suspend fun getAllReports(): Result<List<Report>> {
        return try {
            // Mengambil semua laporan dan mengurutkannya dari yang terbaru
            val snapshot = db.collection("reports")
                .get()
                .await()

            val reports = snapshot.toObjects(Report::class.java).sortedByDescending { it.date }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}