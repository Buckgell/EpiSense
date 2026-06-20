package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AddAlertState {
    object Idle : AddAlertState()
    object Loading : AddAlertState()
    object Success : AddAlertState()
    data class Error(val message: String) : AddAlertState()
}

class AddAlertViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<AddAlertState>(AddAlertState.Idle)
    val uiState: StateFlow<AddAlertState> = _uiState

    fun addAlert(title: String, message: String, severity: String) {
        _uiState.value = AddAlertState.Loading

        // Kita gunakan System.currentTimeMillis() untuk mengisi field date
        val alertData = hashMapOf(
            "title" to title,
            "message" to message,
            "severity" to severity,
            "date" to System.currentTimeMillis()
        )

        db.collection("alerts")
            .add(alertData)
            .addOnSuccessListener {
                _uiState.value = AddAlertState.Success
                // Kembalikan state ke Idle setelah beberapa saat jika diperlukan
                // atau biarkan UI yang meresetnya (seperti yang sudah kita buat di Screen)
            }
            .addOnFailureListener { e ->
                _uiState.value = AddAlertState.Error(e.message ?: "Gagal mengirim peringatan.")
            }
    }
}