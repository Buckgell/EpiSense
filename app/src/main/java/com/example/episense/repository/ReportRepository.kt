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
}