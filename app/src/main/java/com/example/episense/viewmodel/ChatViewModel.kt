package com.example.episense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// PERBAIKAN 1: Menambahkan Anotasi PropertyName agar Firebase tidak bingung
data class ChatMessage(
    val messageId: String = "",
    val text: String = "",
    @get:PropertyName("isUser")
    @set:PropertyName("isUser")
    var isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "" // <-- Jangan lupa masukkan API Key Anda lagi
    )

    // Menyimpan listener agar bisa dimatikan saat logout
    private var chatListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        fetchChatHistory()
    }

    private fun fetchChatHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("ChatViewModel", "User belum login saat memuat chat!")
            return
        }

        chatListener = db.collection("users").document(userId).collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Gagal mengambil chat: ${error.message}")
                    tampilkanSapaanAwalLokal()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val chatHistory = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ChatMessage::class.java)
                        } catch (e: Exception) {
                            null // Abaikan pesan lama yang formatnya rusak
                        }
                    }
                    _messages.value = chatHistory
                } else {
                    // Jika database benar-benar kosong, tampilkan sapaan awal
                    tampilkanSapaanAwalLokal()
                }
            }
    }

    // PERBAIKAN 2: Sapaan awal hanya tampil di layar, tidak dikirim ke Firestore
    private fun tampilkanSapaanAwalLokal() {
        if (_messages.value.isNotEmpty()) return

        val welcomeMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            text = "Halo! Saya asisten AI EpiSense. Ada yang bisa saya bantu terkait informasi kesehatan atau malaria hari ini?",
            isUser = false
        )
        _messages.value = listOf(welcomeMessage)
    }

    private fun saveMessageToFirestore(message: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("chats")
            .document(message.messageId)
            .set(message)
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Gagal menyimpan chat ke Firestore: ${e.message}")
            }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val currentList = _messages.value.toMutableList()

        // 1. Simpan pesan User
        val userMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            text = userText,
            isUser = true
        )
        currentList.add(userMessage)
        _messages.value = currentList
        saveMessageToFirestore(userMessage)

        // 2. Minta AI menjawab
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = "Kamu adalah asisten medis virtual untuk aplikasi EpiSense (sistem pelaporan dan edukasi malaria di Indonesia). Jawab pertanyaan berikut dengan ramah, ringkas, dan mudah dipahami: $userText"
                val response = generativeModel.generateContent(prompt)
                val aiResponseText = response.text ?: "Maaf, saya sedang tidak bisa merespons."

                val aiMessage = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    text = aiResponseText,
                    isUser = false
                )

                val updatedList = _messages.value.toMutableList()
                updatedList.add(aiMessage)
                _messages.value = updatedList
                saveMessageToFirestore(aiMessage)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error Gemini: ${e.message}")
                val errorMessage = ChatMessage(
                    messageId = UUID.randomUUID().toString(),
                    text = "Maaf, sistem AI sedang sibuk. (Error: ${e.localizedMessage})",
                    isUser = false
                )
                val errorList = _messages.value.toMutableList()
                errorList.add(errorMessage)
                _messages.value = errorList
            } finally {
                _isLoading.value = false
            }
        }
    }

    // PERBAIKAN 3: Mematikan Listener saat ViewModel dihancurkan (misal: saat logout)
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}