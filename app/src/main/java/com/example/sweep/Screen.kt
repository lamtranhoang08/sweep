package com.example.sweep

import android.net.Uri
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

/**
 * Route for the full-screen media viewer. It isn't a bottom-nav tab, so it lives outside the [Screen] sealed class
 *
 * A content:// uri contains slahses and colons, which would break the route path,
 * so [create] URL_encodes it. Navigation decodes it again automatically when the argument is read.
 */
object MediaViewerRoute {
    const val ARG_URI = "uri"
    const val ROUTE = "viewer/{$ARG_URI}"

    fun create(uri: Uri): String = "viewer/${Uri.encode(uri.toString())}"
}