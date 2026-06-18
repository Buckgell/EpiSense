package com.example.episense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// PERBAIKAN: Menambahkan messageId dan timestamp, serta default value agar didukung Firestore
data class ChatMessage(
    val messageId: String = "",
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // API Key Anda sudah dimasukkan (Ingat untuk tidak push API Key asli ke GitHub publik!)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "input api key"
    )

    init {
        fetchChatHistory()
    }

    // Mengambil riwayat chat dari Firestore
    private fun fetchChatHistory() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatHistory = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }

                    // Jika belum ada chat sama sekali, kirim sapaan awal ke database
                    if (chatHistory.isEmpty()) {
                        val welcomeMessage = ChatMessage(
                            messageId = UUID.randomUUID().toString(),
                            text = "Halo! Saya asisten AI EpiSense HITAM. Ada yang bisa saya bantu terkait informasi kesehatan atau malaria hari ini?",
                            isUser = false
                        )
                        saveMessageToFirestore(welcomeMessage)
                    } else {
                        _messages.value = chatHistory
                    }
                }
            }
    }

    // Fungsi utilitas untuk menyimpan pesan ke Firestore
    private fun saveMessageToFirestore(message: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("chats")
            .document(message.messageId)
            .set(message)
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Simpan pesan User ke Firestore (UI akan otomatis update dari SnapshotListener)
        val userMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            text = userText,
            isUser = true
        )
        saveMessageToFirestore(userMessage)

        // 2. Kirim ke Gemini
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Konteks aplikasi
                val prompt = "Kamu adalah asisten medis virtual untuk aplikasi EpiSense (sistem pelaporan dan edukasi malaria di Indonesia). Jawab pertanyaan berikut dengan ramah, ringkas, dan mudah dipahami: $userText"

                val response = generativeModel.generateContent(prompt)
                val aiResponseText = response.text ?: "Maaf, saya sedang tidak bisa merespons. Coba lagi nanti."

                // 3. Simpan balasan AI ke Firestore
                val aiMessage = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    text = aiResponseText,
                    isUser = false
                )
                saveMessageToFirestore(aiMessage)

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    text = "Error asli: ${e.localizedMessage ?: "Pesan error kosong dari sistem"}",
                    isUser = false
                )
                saveMessageToFirestore(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }
}