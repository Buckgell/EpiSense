package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // GANTI "PASTE_API_KEY_ANDA_DI_SINI" dengan API Key dari Google AI Studio!
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "PASTE_API_KEY_ANDA_DI_SINI"
    )

    init {
        // Pesan sapaan awal dari AI
        _messages.value = listOf(
            ChatMessage("Halo! Saya asisten AI EpiSense. Ada yang bisa saya bantu terkait informasi kesehatan atau malaria hari ini?", isUser = false)
        )
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Tambahkan pesan user ke layar
        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage(userText, isUser = true))
        _messages.value = currentList

        // 2. Kirim ke Gemini
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Beri konteks agar jawaban Gemini selalu relevan dengan aplikasi Anda
                val prompt = "Kamu adalah asisten medis virtual untuk aplikasi EpiSense (sistem pelaporan dan edukasi malaria di Indonesia). Jawab pertanyaan berikut dengan ramah, ringkas, dan mudah dipahami: $userText"

                val response = generativeModel.generateContent(prompt)
                val aiResponse = response.text ?: "Maaf, saya sedang tidak bisa merespons. Coba lagi nanti."

                val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage(aiResponse, isUser = false))
                _messages.value = updatedList
            } catch (e: Exception) {
                val errorList = _messages.value.toMutableList()
                errorList.add(ChatMessage("Error: Koneksi bermasalah atau API Key tidak valid.", isUser = false))
                _messages.value = errorList
            } finally {
                _isLoading.value = false
            }
        }
    }
}