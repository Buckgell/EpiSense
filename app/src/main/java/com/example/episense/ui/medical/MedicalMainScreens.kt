package com.example.episense.ui.medical

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MedicalMainScreen(onLogoutSuccess: () -> Unit = {}) {
    val navController = rememberNavController()

    val items = listOf(
        MedicalNavItem.Dashboard,
        MedicalNavItem.Analytics,
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
                                // Mencegah penumpukan stack saat berpindah tab
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


            composable(MedicalNavItem.Analytics.route) { AnalyticsScreen() }
            composable(MedicalNavItem.AddEducation.route) { MedicalAddEducationScreen() }
            composable(MedicalNavItem.AddAlert.route) { MedicalAddAlertScreen() }

            // Profil dengan parameter logout yang sudah diperbaiki
            composable(MedicalNavItem.Profile.route) {
                com.example.episense.ui.profile.ProfileScreen(onNavigateToLogin = onLogoutSuccess)
            }

            // Rute Peta (Map)
            composable("map_screen") {
                com.example.episense.ui.map.MapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}