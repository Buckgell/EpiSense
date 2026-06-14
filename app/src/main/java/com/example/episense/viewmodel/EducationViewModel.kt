package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.Education
import com.example.episense.repository.EducationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EducationState {
    object Loading : EducationState()
    data class Success(val educations: List<Education>) : EducationState()
    data class Error(val message: String) : EducationState()
}

class EducationViewModel : ViewModel() {
    private val repository = EducationRepository()

    private val _uiState = MutableStateFlow<EducationState>(EducationState.Loading)
    val uiState: StateFlow<EducationState> = _uiState

    init {
        fetchEducations()
    }

    private fun fetchEducations() {
        viewModelScope.launch {
            _uiState.value = EducationState.Loading
            val result = repository.getEducations()
            if (result.isSuccess) {
                _uiState.value = EducationState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = EducationState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat data")
            }
        }
    }
}