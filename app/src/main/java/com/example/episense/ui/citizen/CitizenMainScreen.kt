package com.example.episense.ui.citizen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : BottomNavItem("home_tab", "Home", Icons.Filled.Home)
    object Education : BottomNavItem("education_tab", "Edukasi", Icons.Filled.Info)
    object Report : BottomNavItem("report_tab", "Lapor", Icons.Filled.AddCircle)
    object Alert : BottomNavItem("alert_tab", "Alert", Icons.Filled.Warning)
    object AI : BottomNavItem("ai_tab", "AI", Icons.Filled.Face) // Diganti Face agar tidak bentrok dengan Profil
    object Profile : BottomNavItem("profile_tab", "Profil", Icons.Filled.AccountCircle) // Tambahan Tab Profil
}

@Composable
// Tambahan parameter onLogoutSuccess
fun CitizenMainScreen(onLogoutSuccess: () -> Unit = {}) {
    val navController = rememberNavController()

    // Tambahkan Profile ke dalam daftar items agar muncul di bawah
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Education,
        BottomNavItem.Report,
        BottomNavItem.Alert,
        BottomNavItem.AI,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Education.route) { EducationScreen() }

        // 1. Ubah bagian ReportScreen agar menerima perintah navigasi
        composable(BottomNavItem.Report.route) {
            ReportScreen(onNavigateToHistory = { navController.navigate("my_reports") })
        }

        // 2. Gunakan String langsung ("my_reports"), BUKAN BottomNavItem
        composable("my_reports") { MyReportsScreen() }

        composable(BottomNavItem.Alert.route) { AlertScreen() }
        composable(BottomNavItem.AI.route) { AIScreen() }
        composable(BottomNavItem.Profile.route) {
            com.example.episense.ui.profile.ProfileScreen(onLogoutSuccess = onLogoutSuccess)
        }
    }
    }
}