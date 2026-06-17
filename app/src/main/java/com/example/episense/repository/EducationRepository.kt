package com.example.episense.repository

import com.example.episense.model.Education
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EducationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("educations")

    suspend fun getEducations(): Result<List<Education>> {
        return try {
            val snapshot = collection.get().await()

            // Jika kosong, kita buatkan data awal otomatis
            if (snapshot.isEmpty) {
                seedInitialData()
                val newSnapshot = collection.get().await()
                Result.success(newSnapshot.toObjects(Education::class.java))
            } else {
                Result.success(snapshot.toObjects(Education::class.java))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedInitialData() {
        val dummyData = listOf(
            Education("1", "Pengertian Malaria", "Malaria adalah penyakit mematikan yang ditularkan melalui gigitan nyamuk Anopheles betina yang terinfeksi parasit Plasmodium.", ""),
            Education("2", "Gejala Awal", "Gejala utama meliputi demam tinggi, menggigil, sakit kepala, mual, dan muntah yang biasanya muncul 10-15 hari setelah gigitan.", ""),
            Education("3", "Pencegahan", "Gunakan kelambu saat tidur, semprotan anti nyamuk, dan bersihkan genangan air di sekitar rumah untuk memutus siklus hidup nyamuk.", "")
        )

        val batch = db.batch()
        dummyData.forEach { edu ->
            val docRef = collection.document(edu.educationId)
            batch.set(docRef, edu)
        }
        batch.commit().await()
    }
    // --- TAMBAHKAN FUNGSI INI UNTUK MEDICAL STAFF ---
    suspend fun addEducation(title: String, content: String): Result<Boolean> {
        return try {
            val educationId = java.util.UUID.randomUUID().toString()

            // Kita buat map data agar otomatis menyesuaikan dengan struktur Firestore Anda
            val newEducation = hashMapOf(
                "id" to educationId,
                "title" to title,
                "content" to content,
                "author" to "Tenaga Medis", // Bisa diganti mengambil nama user yang login
                "date" to System.currentTimeMillis() // Menggunakan timestamp
            )

            // Simpan ke collection "educations"
            db.collection("educations").document(educationId).set(newEducation).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}