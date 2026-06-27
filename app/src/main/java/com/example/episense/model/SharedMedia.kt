package com.example.episense.model

data class SharedMedia(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val mediaType: String = "image", // "image" atau "video"
    val uploaderName: String = "",
    val uploaderRole: String = "",
    val timestamp: Long = 0L
)