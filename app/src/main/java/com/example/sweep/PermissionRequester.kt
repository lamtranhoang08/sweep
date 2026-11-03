package com.example.sweep

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** All three are needed: the library, duplicate scan and totals cover every media type. */
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.READ_MEDIA_VIDEO,
    Manifest.permission.READ_MEDIA_AUDIO
)

private fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun hasAllMediaAccess(context: Context): Boolean =
    REQUIRED_PERMISSIONS.all { isGranted(context, it) }

/** Compose gives us a wrapped Context; the permission APIs need the Activity inside it. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

/**
 * Gate that lets the app through once media access is granted.
 *
 * It asks for access straight away and draws nothing while the system dialog
 * is up, so a user who allows everything never sees an explanation. Only
 * after a denial does it explain why access is needed, offer to ask again,
 * and, once Android stops showing its dialog, send the user to Settings. It
 * continues on its own when access is granted, including from Settings.
 */
@Composable
fun PermissionRequester(onGranted: () -> Unit) {
    val context = LocalContext.current

    // The real permission state, re-read whenever it could have changed.
    var hasPermission by remember { mutableStateOf(hasAllMediaAccess(context)) }

    // Saved so rotation neither re-asks nor forgets a denial.
    var hasAsked by rememberSaveable { mutableStateOf(false) }
    var showExplanation by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = hasAllMediaAccess(context)

        // An empty result means the request was interrupted (for example by a
        // rotation), not that the user said no, so there is nothing to explain yet.
        if (!hasPermission && results.isNotEmpty()) {
            showExplanation = true

            // "Rationale" turns false once Android will no longer show its dialog
            // for a permission the user has denied. Only count an explicit denial.
            val activity = context.findActivity()
            permanentlyDenied = activity != null && REQUIRED_PERMISSIONS.any { permission ->
                results[permission] == false &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
        }
    }

    // Ask once, immediately, without an explanation screen first.
    LaunchedEffect(Unit) {
        if (!hasPermission && !hasAsked) {
            hasAsked = true
            launcher.launch(REQUIRED_PERMISSIONS)
        }
    }

    // Returning from Settings: pick up any change made there.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasPermission = hasAllMediaAccess(context)
    }

    // Continue into Sweep as soon as access is granted.
    LaunchedEffect(hasPermission) {
        if (hasPermission) onGranted()
    }

    // The explanation only ever appears after a denial.
    if (!hasPermission && showExplanation) {
        PermissionExplanation(
            permanentlyDenied = permanentlyDenied,
            onActionClick = {
                if (permanentlyDenied) openAppSettings(context)
                else launcher.launch(REQUIRED_PERMISSIONS)
            }
        )
    }
}

// TODO: Move user-facing strings into strings.xml (needed for localization).
@Composable
private fun PermissionExplanation(
    permanentlyDenied: Boolean,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sweep needs access to your media",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (permanentlyDenied) {
                "Access is turned off. Open settings, choose Permissions, and allow " +
                        "Photos and videos and Music and audio."
            } else {
                "Sweep needs your photos, videos, and audio to show your library, " +
                        "find duplicates, and measure storage. Everything stays on your device."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onActionClick) {
            Text(if (permanentlyDenied) "Open settings" else "Allow access")
        }
    }
}