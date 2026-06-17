package com.example.episense

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.episense.ui.citizen.CitizenMainScreen
import com.example.episense.ui.auth.LoginScreen
import com.example.episense.ui.auth.RegisterScreen
import com.example.episense.ui.theme.EpiSenseTheme
import com.example.episense.utils.SeederManager
import com.example.episense.viewmodel.AuthState
import com.example.episense.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seeder sudah dimatikan akses write-nya di Firestore, jadi aman dibiarkan di sini
        SeederManager.seedDataIfNeeded()

        enableEdgeToEdge()
        setContent {
            EpiSenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()
                    val context = LocalContext.current

                    // Pantau status Autentikasi
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Success -> {
                                val user = (authState as AuthState.Success).user
                                Toast.makeText(context, "Berhasil Login sebagai ${user.role}", Toast.LENGTH_SHORT).show()
                                authViewModel.resetState() // Reset agar tidak terjadi navigasi berulang

                                // Pengecekan Role
                                if (user.role == "medical_staff") {
                                    navController.navigate("medical_home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                            is AuthState.Error -> {
                                val errorMessage = (authState as AuthState.Error).message
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                authViewModel.resetState()
                            }
                            else -> {}
                        }
                    }

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginClick = { email, password ->
                                    authViewModel.login(email, password)
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = { navController.popBackStack() },
                                onRegisterClick = { name, email, password ->
                                    authViewModel.register(name, email, password)
                                }
                            )
                        }
                        composable("home") {
                            CitizenMainScreen()
                        }
                        composable("medical_home") {
                            com.example.episense.ui.medical.MedicalMainScreen()
                        }
                    }
                }
            }
        }
    }
}