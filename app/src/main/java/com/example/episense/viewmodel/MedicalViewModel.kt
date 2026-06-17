package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.Report
import com.example.episense.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MedicalState {
    object Loading : MedicalState()
    data class Success(val reports: List<Report>) : MedicalState()
    data class Error(val message: String) : MedicalState()
}

class MedicalViewModel : ViewModel() {
    private val repository = ReportRepository()

    private val _uiState = MutableStateFlow<MedicalState>(MedicalState.Loading)
    val uiState: StateFlow<MedicalState> = _uiState

    init {
        fetchReports()
    }

    private fun fetchReports() {
        viewModelScope.launch {
            _uiState.value = MedicalState.Loading
            val result = repository.getAllReports()

            if (result.isSuccess) {
                _uiState.value = MedicalState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = MedicalState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat laporan")
            }
        }
    }
}