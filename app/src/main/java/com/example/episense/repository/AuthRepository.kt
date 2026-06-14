package com.example.episense.repository

import com.example.episense.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Fungsi Register
    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // 1. Buat user di Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 2. Simpan data tambahan ke Firestore
                val user = User(
                    userId = firebaseUser.uid,
                    name = name,
                    email = email,
                    role = "citizen" // Sesuai spesifikasi, default adalah citizen
                )

                db.collection("users").document(firebaseUser.uid).set(user).await()
                Result.success(user)
            } else {
                Result.failure(Exception("Gagal membuat user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi Login
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 1. Login dengan Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 2. Ambil data role user dari Firestore
                val document = db.collection("users").document(firebaseUser.uid).get().await()
                val user = document.toObject(User::class.java)

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Data user tidak ditemukan di database"))
                }
            } else {
                Result.failure(Exception("Gagal login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cek apakah user sedang login
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    // Fungsi Logout
    fun logout() {
        auth.signOut()
    }
}