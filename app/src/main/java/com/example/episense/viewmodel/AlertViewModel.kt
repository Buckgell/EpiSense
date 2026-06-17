package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.Alert
import com.example.episense.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AlertState {
    object Loading : AlertState()
    data class Success(val alerts: List<Alert>) : AlertState()
    data class Error(val message: String) : AlertState()
}

class AlertViewModel : ViewModel() {
    private val repository = AlertRepository()

    private val _uiState = MutableStateFlow<AlertState>(AlertState.Loading)
    val uiState: StateFlow<AlertState> = _uiState

    init {
        fetchAlerts()
    }

    // Fungsi diubah menjadi PUBLIC agar bisa dipanggil oleh tombol Refresh di UI
    fun fetchAlerts() {
        viewModelScope.launch {
            _uiState.value = AlertState.Loading
            val result = repository.getAlerts()
            if (result.isSuccess) {
                _uiState.value = AlertState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = AlertState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat peringatan")
            }
        }
    }
}