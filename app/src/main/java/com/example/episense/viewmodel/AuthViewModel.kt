package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.User
import com.example.episense.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

// Menyimpan status/state layar saat ini
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login Gagal")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(name, email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Register Gagal")
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLogoutSuccess()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}