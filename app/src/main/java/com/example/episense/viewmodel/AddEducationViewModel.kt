package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.repository.EducationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AddEducationState {
    object Idle : AddEducationState()
    object Loading : AddEducationState()
    object Success : AddEducationState()
    data class Error(val message: String) : AddEducationState()
}

class AddEducationViewModel : ViewModel() {
    private val repository = EducationRepository()

    private val _uiState = MutableStateFlow<AddEducationState>(AddEducationState.Idle)
    val uiState: StateFlow<AddEducationState> = _uiState

    fun addEducation(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            _uiState.value = AddEducationState.Error("Judul dan isi artikel tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddEducationState.Loading
            val result = repository.addEducation(title, content)

            if (result.isSuccess) {
                _uiState.value = AddEducationState.Success
            } else {
                _uiState.value = AddEducationState.Error(result.exceptionOrNull()?.message ?: "Gagal menambah edukasi")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddEducationState.Idle
    }
}