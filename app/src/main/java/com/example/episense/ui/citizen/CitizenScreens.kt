package com.example.episense.ui.citizen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Home (Ringkasan & Alert)")
    }
}

@Composable
fun EducationScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Edukasi Malaria")
    }
}

@Composable
fun ReportScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Form Laporan Gejala")
    }
}

@Composable
fun AlertScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Daftar Alert/Peringatan")
    }
}

@Composable
fun AIScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Halaman Chatbot AI Gemini")
    }
}