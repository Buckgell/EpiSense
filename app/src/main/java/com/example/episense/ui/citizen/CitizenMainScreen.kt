package com.example.episense.ui.citizen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : BottomNavItem("home_tab", "Home", Icons.Filled.Home)
    object Education : BottomNavItem("education_tab", "Edukasi", Icons.Filled.Info)
    object Report : BottomNavItem("report_tab", "Lapor", Icons.Filled.AddCircle)
    object Alert : BottomNavItem("alert_tab", "Alert", Icons.Filled.Warning)
    object AI : BottomNavItem("ai_tab", "AI", Icons.Filled.Face)
    object Profile : BottomNavItem("profile_tab", "Profil", Icons.Filled.AccountCircle)
}

@Composable
fun CitizenMainScreen(onLogoutSuccess: () -> Unit = {}) {
    val navController = rememberNavController()

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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // PERBAIKAN: Berikan akses navigasi peta ke HomeScreen
            composable(BottomNavItem.Home.route) {
                HomeScreen(onNavigateToMap = { navController.navigate("map_screen") })
            }
            composable(BottomNavItem.Education.route) { EducationScreen() }
            composable(BottomNavItem.Report.route) {
                ReportScreen(onNavigateToHistory = { navController.navigate("my_reports") })
            }
            composable("my_reports") { MyReportsScreen() }

            // PERBAIKAN: Daftarkan layar peta di sini
            composable("map_screen") {
                com.example.episense.ui.map.MapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Alert.route) { AlertScreen() }
            composable(BottomNavItem.AI.route) { AIScreen() }
            composable(BottomNavItem.Profile.route) {
                // Ubah nama parameternya menjadi onNavigateToLogin
                com.example.episense.ui.profile.ProfileScreen(onNavigateToLogin = onLogoutSuccess)
            }
        }
    }
}