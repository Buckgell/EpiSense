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
    object AI : BottomNavItem("ai_tab", "AI", Icons.Filled.Person) // Menggunakan Person sementara untuk AI
}

@Composable
fun CitizenMainScreen() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Education,
        BottomNavItem.Report,
        BottomNavItem.Alert,
        BottomNavItem.AI
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
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Education.route) { EducationScreen() }
            composable(BottomNavItem.Report.route) { ReportScreen() }
            composable(BottomNavItem.Alert.route) { AlertScreen() }
            composable(BottomNavItem.AI.route) { AIScreen() }
        }
    }
}