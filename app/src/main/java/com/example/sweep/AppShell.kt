package com.example.sweep

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sweep.screens.BrowseScreen
import com.example.sweep.screens.CleanupScreen
import com.example.sweep.screens.GalleryScreen
import com.example.sweep.screens.MediaViewerScreen
import com.example.sweep.screens.StorageScreen

/**
 * App-level scaffold: bottom navigation for the four main tabs, plus the
 * NavHost that swaps screens. The full-screen media viewer is also routed
 * from here but is not a tab, so the bottom bar is hidden while it shows.
 */
@Composable
fun AppShell() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()
        .value?.destination?.route

    // Show the bottom bar only on the four main tabs.
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
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
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Browse.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Browse.route) { BrowseScreen() }

            composable(Screen.Gallery.route) {
                GalleryScreen(
                    onItemClick = { item ->
                        navController.navigate(MediaViewerRoute.create(item.uri))
                    }
                )
            }

            composable(Screen.Storage.route) { StorageScreen() }
            composable(Screen.Cleanup.route) { CleanupScreen() }

            composable(
                route = MediaViewerRoute.ROUTE,
                arguments = listOf(
                    navArgument(MediaViewerRoute.ARG_URI) { type = NavType.StringType }
                )
            ) { entry ->
                val uriString = entry.arguments?.getString(MediaViewerRoute.ARG_URI)
                    ?: return@composable
                MediaViewerScreen(
                    uri = Uri.parse(uriString),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}