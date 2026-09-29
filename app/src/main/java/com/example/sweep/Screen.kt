package com.example.sweep

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Browse : Screen("browse", "Browse", Icons.Default.Folder)
    object Gallery : Screen("gallery", "Gallery", Icons.Default.Photo)
    object Storage : Screen("storage", "Storage", Icons.Default.PieChart)
    object Cleanup : Screen("cleanup", "Cleanup", Icons.Default.CleaningServices)
}

val bottomNavItems = listOf(Screen.Browse, Screen.Gallery, Screen.Storage, Screen.Cleanup)