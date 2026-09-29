package com.example.sweep

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sweep.screens.BrowseScreen
import com.example.sweep.screens.CleanupScreen
import com.example.sweep.screens.GalleryScreen
import com.example.sweep.screens.StorageScreen

@Composable
fun AppShell() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState()
                    .value?.destination?.route
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Browse.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Browse.route) { BrowseScreen() }
            composable(Screen.Gallery.route) { GalleryScreen() }
            composable(Screen.Storage.route) { StorageScreen() }
            composable(Screen.Cleanup.route) { CleanupScreen() }
        }
    }
}