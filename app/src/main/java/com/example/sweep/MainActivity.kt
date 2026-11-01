package com.example.sweep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import com.example.sweep.ui.theme.SweepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // One AppConatiner shared across the whole app process
        // owned by SweepApplication
        val container = (application as SweepApplication).container

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    var granted by remember { mutableStateOf(false) }

    if (granted) {
        AppShell()
    } else {
        PermissionRequester(onGranted = { granted = true })
    }
}