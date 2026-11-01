package com.example.sweep.screens.components

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.sweep.data.FileItem
import com.example.sweep.util.createDeleteIntentSender
import com.example.sweep.util.createWriteIntentSender
import com.example.sweep.util.renameFile
import com.example.sweep.util.shareFile

/** A rename that is waiting for the user to approve the system write request */
private data class PendingRename(val file: FileItem, val newName: String)

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
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var pendingRename by remember { mutableStateOf<PendingRename?>(null) }

    // Receives the outcome of the system's delete-confirmation dialog. This
    // must be created on every composition, even when no file is selected.
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
        }
    }

    val writeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = pendingRename
        pendingRename = null
        if (result.resultCode == Activity.RESULT_OK && pending != null) {
            val renamed = renameFile(context, pending.file, pending.newName)
            val message = if (renamed) "Renamed" else "Couldn't rename. Try a different name."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
            onRename = {
                renameTarget = target
                onDismiss()
            },
            onDelete = {
                deleteLauncher.launch(
                    IntentSenderRequest.Builder(createDeleteIntentSender(context, target)).build()
                )
                onDismiss()
            }
        )
    }

    renameTarget?.let { target ->
        RenameDialog(
            file = target,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                renameTarget = null
                pendingRename = PendingRename(target, newName)
                writeLauncher.launch(
                    IntentSenderRequest.Builder(createWriteIntentSender(context, target)).build()
                )
            }
        )
    }
}