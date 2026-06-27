package com.example.episense.ui.shared

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.episense.model.SharedMedia
import com.example.episense.viewmodel.MediaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMediaScreen(
    onNavigateBack: () -> Unit,
    currentUserRole: String,
    currentUserName: String,
    viewModel: MediaViewModel = viewModel()
) {
    val mediaList by viewModel.mediaList.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri Edukasi & Media") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Kembali") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Media", tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            if (mediaList.isEmpty()) {
                item { Text("Belum ada media yang dibagikan.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
            } else {
                items(mediaList) { media ->
                    MediaCard(media) { url ->
                        // Buka foto/video menggunakan browser bawaan HP
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var selectedUri by remember { mutableStateOf<Uri?>(null) }
        var selectedMediaType by remember { mutableStateOf("") }
        var isUploading by remember { mutableStateOf(false) }

        // Membuka galeri bawaan HP (Bisa pilih gambar ATAU video)
        val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedUri = uri
                val mimeType = context.contentResolver.getType(uri)
                selectedMediaType = if (mimeType?.startsWith("video") == true) "video" else "image"
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isUploading) showDialog = false },
            title = { Text("Upload Foto / Video") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Media") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            mediaPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading
                    ) {
                        Text(if (selectedUri == null) "Pilih dari Galeri" else "File Dipilih (${selectedMediaType})")
                    }

                    if (isUploading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Mengupload media, harap tunggu...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isUploading = true
                        viewModel.uploadMediaFile(
                            fileUri = selectedUri!!,
                            title = title,
                            mediaType = selectedMediaType,
                            uploaderName = currentUserName,
                            uploaderRole = currentUserRole,
                            onSuccess = {
                                isUploading = false
                                showDialog = false
                                Toast.makeText(context, "Upload Berhasil!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                isUploading = false
                                Toast.makeText(context, "Gagal: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = title.isNotBlank() && selectedUri != null && !isUploading
                ) { Text("Bagikan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }, enabled = !isUploading) { Text("Batal") }
            }
        )
    }
}

@Composable
fun MediaCard(media: SharedMedia, onClick: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(media.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick(media.url) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ikon akan berbeda jika itu video atau gambar
            val icon = if (media.mediaType == "video") Icons.Default.PlayArrow else androidx.compose.material.icons.Icons.Filled.Info
            Icon(icon, contentDescription = "Media", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(media.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Oleh: ${media.uploaderName} (${media.uploaderRole})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(dateString, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}