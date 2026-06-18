package com.example.episense.ui.medical

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
// Tambahan parameter onLogoutSuccess
fun MedicalMainScreen(onLogoutSuccess: () -> Unit = {}) {
    val navController = rememberNavController()
    // Masukkan MedicalNavItem.Profile ke dalam list
    val items = listOf(
        MedicalNavItem.Dashboard,
        MedicalNavItem.AddEducation,
        MedicalNavItem.AddAlert,
        MedicalNavItem.Profile
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
            startDestination = MedicalNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MedicalNavItem.Dashboard.route) { MedicalDashboardScreen() }
            composable(MedicalNavItem.AddEducation.route) { MedicalAddEducationScreen() }
            composable(MedicalNavItem.AddAlert.route) { MedicalAddAlertScreen() }
            composable(MedicalNavItem.Analytics.route) { AnalyticsScreen() }
            // Tambahan rute untuk layar profil
            composable(MedicalNavItem.Profile.route) {
                com.example.episense.ui.profile.ProfileScreen(onLogoutSuccess = onLogoutSuccess)
            }
        }
    }
}