package com.example.episense.utils

import android.util.Log
import com.example.episense.model.HistoricalCase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

object SeederManager {
    private const val TAG = "SeederManager"
    private val db = FirebaseFirestore.getInstance()

    fun seedDataIfNeeded() {
        val collectionRef = db.collection("historical_cases")

        // Cek apakah data sudah ada
        collectionRef.limit(1).get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                Log.d(TAG, "Collection kosong. Memulai seeding data...")
                generateAndUploadDummyData()
            } else {
                Log.d(TAG, "Data sudah ada, seeding dilewati.")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Gagal mengecek collection: ", e)
        }
    }

    private fun generateAndUploadDummyData() {
        val dummyData = mutableListOf<HistoricalCase>()
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 1) // Mulai dari 1 Jan 2025

        val provinces = listOf("Jawa Timur", "Papua", "Nusa Tenggara Timur")
        val cities = mapOf(
            "Jawa Timur" to "Surabaya",
            "Papua" to "Jayapura",
            "Nusa Tenggara Timur" to "Kupang"
        )

        // Generate sekitar 500 data (simulasi per hari selama ~1.5 tahun)
        for (i in 0..500) {
            val province = provinces.random()
            val city = cities[province] ?: ""

            // Variabel Musiman & Tren
            val month = calendar.get(Calendar.MONTH)
            val baseCases = if (month in 10..11 || month in 0..2) {
                // Musim Hujan (Nov-Mar) -> Kasus lebih tinggi
                Random.nextInt(50, 150)
            } else {
                // Musim Kemarau -> Kasus lebih rendah
                Random.nextInt(10, 50)
            }

            // Simulasi Outbreak (Lonjakan Wabah) probabilitas 5%
            val isOutbreak = Random.nextDouble() < 0.05
            val caseCount = if (isOutbreak) baseCases * Random.nextInt(3, 5) else baseCases + Random.nextInt(-10, 10)

            // Noise Data untuk cuaca
            val rainfall = if (month in 10..11 || month in 0..2) Random.nextDouble(100.0, 300.0) else Random.nextDouble(0.0, 50.0)
            val temperature = Random.nextDouble(25.0, 35.0)
            val humidity = Random.nextDouble(60.0, 95.0)

            val historicalCase = HistoricalCase(
                caseId = UUID.randomUUID().toString(),
                province = province,
                city = city,
                date = calendar.timeInMillis,
                caseCount = maxOf(0, caseCount), // Tidak boleh negatif
                rainfall = rainfall,
                temperature = temperature,
                humidity = humidity,
                populationDensity = Random.nextInt(500, 5000)
            )

            dummyData.add(historicalCase)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        uploadInBatches(dummyData)
    }

    private fun uploadInBatches(data: List<HistoricalCase>) {
        val batch = db.batch()
        val collectionRef = db.collection("historical_cases")

        // Firestore batch maksimal 500 operasi, karena data kita ~501, kita ambil 500 saja agar aman dalam 1 batch
        data.take(500).forEach { item ->
            val docRef = collectionRef.document(item.caseId)
            batch.set(docRef, item)
        }

        batch.commit().addOnSuccessListener {
            Log.d(TAG, "Berhasil mengunggah 500 dummy data kasus historis!")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Gagal mengunggah dummy data: ", e)
        }
    }
}