package com.example.episense.model

data class ChatMessage(
    val messageId: String = "",
    val text: String = "",
    val isFromUser: Boolean = true, // true jika pesan dari warga, false jika balasan dari AI
    val timestamp: Long = System.currentTimeMillis()
)