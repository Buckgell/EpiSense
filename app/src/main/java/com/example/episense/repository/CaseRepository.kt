package com.example.episense.repository

import com.example.episense.model.HistoricalCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getRecentCases(): Result<List<HistoricalCase>> {
        return try {
            // Mengambil 30 data terbaru berdasarkan tanggal
            val snapshot = db.collection("historical_cases")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()

            Result.success(snapshot.toObjects(HistoricalCase::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}