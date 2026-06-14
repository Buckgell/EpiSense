package com.example.episense.repository

import com.example.episense.model.Alert
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AlertRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("alerts")

    suspend fun getAlerts(): Result<List<Alert>> {
        return try {
            val snapshot = collection.get().await()

            if (snapshot.isEmpty) {
                seedInitialData()
                val newSnapshot = collection.get().await()
                Result.success(newSnapshot.toObjects(Alert::class.java).sortedByDescending { it.date })
            } else {
                Result.success(snapshot.toObjects(Alert::class.java).sortedByDescending { it.date })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedInitialData() {
        val dummyAlert1 = Alert(
            alertId = "alert_1",
            title = "Waspada Musim Hujan",
            message = "Peningkatan kasus nyamuk malaria terpantau. Harap gunakan kelambu dan kurangi genangan air di sekitar rumah Anda.",
            severity = "Medium"
        )
        val dummyAlert2 = Alert(
            alertId = "alert_2",
            title = "Status Siaga Area Timur",
            message = "Ditemukan lonjakan laporan pasien dengan gejala mirip malaria di daerah timur. Segera lapor jika Anda mengalami demam tinggi berhari-hari.",
            severity = "High"
        )

        val batch = db.batch()
        batch.set(collection.document(dummyAlert1.alertId), dummyAlert1)
        batch.set(collection.document(dummyAlert2.alertId), dummyAlert2)
        batch.commit().await()
    }
}