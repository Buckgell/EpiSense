package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.episense.model.Alert
import com.example.episense.model.HistoricalCase
import com.example.episense.repository.AlertRepository
import com.example.episense.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(
        val latestAlert: Alert?,
        val totalRecentCases: Int,
        val recentCasesList: List<HistoricalCase>
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel : ViewModel() {
    private val caseRepository = CaseRepository()
    private val alertRepository = AlertRepository()

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading

            val casesResult = caseRepository.getRecentCases()
            val alertsResult = alertRepository.getAlerts()

            if (casesResult.isSuccess && alertsResult.isSuccess) {
                val cases = casesResult.getOrNull() ?: emptyList()
                val alerts = alertsResult.getOrNull() ?: emptyList()

                // Hitung total kasus dari 30 data terakhir
                val totalCases = cases.sumOf { it.caseCount }
                val latestAlert = alerts.firstOrNull() // Ambil alert paling baru

                _uiState.value = HomeState.Success(
                    latestAlert = latestAlert,
                    totalRecentCases = totalCases,
                    recentCasesList = cases
                )
            } else {
                _uiState.value = HomeState.Error("Gagal memuat data dashboard")
            }
        }
    }
}