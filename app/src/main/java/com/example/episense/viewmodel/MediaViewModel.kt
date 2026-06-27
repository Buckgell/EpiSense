package com.example.episense.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.episense.model.SharedMedia
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class MediaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _mediaList = MutableStateFlow<List<SharedMedia>>(emptyList())
    val mediaList: StateFlow<List<SharedMedia>> = _mediaList

    init {
        fetchMedia()
    }

    private fun fetchMedia() {
        db.collection("shared_media")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(SharedMedia::class.java) }
                    _mediaList.value = list
                }
            }
    }

    // Fungsi Upload File ke Firebase Storage
    fun uploadMediaFile(
        fileUri: Uri,
        title: String,
        mediaType: String,
        uploaderName: String,
        uploaderRole: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val id = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("shared_media/$id")

        // 1. Upload file ke Storage
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // 2. Jika sukses, ambil URL Download-nya
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val media = SharedMedia(
                        id = id,
                        title = title,
                        url = downloadUrl.toString(),
                        mediaType = mediaType,
                        uploaderName = uploaderName,
                        uploaderRole = uploaderRole,
                        timestamp = System.currentTimeMillis()
                    )
                    // 3. Simpan data + URL ke Firestore
                    db.collection("shared_media").document(id).set(media)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Gagal menyimpan data") }
                }.addOnFailureListener { e -> onError(e.message ?: "Gagal mendapatkan URL") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal mengupload file") }
    }
}