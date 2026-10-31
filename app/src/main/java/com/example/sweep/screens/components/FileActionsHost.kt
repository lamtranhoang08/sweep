package com.example.sweep.screens.components

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.sweep.data.FileItem
import com.example.sweep.util.createDeleteIntentSender
import com.example.sweep.util.shareFile

/**
 * Shows the actions sheet for file and carries out whatever the user picks.
 *
 * Screens only track which file is selected (null = nothing selected) and
 * pass it here, so the launchers, dialogs and toasts for file actions live
 * in exactly one place.
 *
 * @param file The selected file, or null to show nothing.
 * @param onDismiss Called when the sheet closes or an action has been started
 */
@Composable
fun FileActionsHost(
    file: FileItem?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Receives the outcome of the system's delete-confirmation dialog. This
    // must be created on every composition, even when no file is selected.
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
        }
    }

    file?.let { target ->
        FileActionsSheet(
            file = target,
            onDismiss = onDismiss,
            onShare = {
                shareFile(context, target)
                onDismiss()
            },
            // TODO: Wire up rename (createWriteRequest + a rename dialog).
            onRename = onDismiss,
            onDelete = {
                deleteLauncher.launch(
                    IntentSenderRequest.Builder(createDeleteIntentSender(context, target)).build()
                )
                onDismiss()
            }
        )
    }
}