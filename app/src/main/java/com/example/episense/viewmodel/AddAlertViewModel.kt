package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddAlertState {
    object Idle : AddAlertState()
    object Loading : AddAlertState()
    object Success : AddAlertState()
    data class Error(val message: String) : AddAlertState()
}

class AddAlertViewModel : ViewModel() {
    private val repository = AlertRepository()

    private val _uiState = MutableStateFlow<AddAlertState>(AddAlertState.Idle)
    val uiState: StateFlow<AddAlertState> = _uiState

    fun sendAlert(title: String, message: String) {
        if (title.isBlank() || message.isBlank()) {
            _uiState.value = AddAlertState.Error("Judul dan pesan tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddAlertState.Loading
            val result = repository.sendAlert(title, message)

            if (result.isSuccess) {
                _uiState.value = AddAlertState.Success
            } else {
                _uiState.value = AddAlertState.Error(result.exceptionOrNull()?.message ?: "Gagal mengirim peringatan")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddAlertState.Idle
    }
}