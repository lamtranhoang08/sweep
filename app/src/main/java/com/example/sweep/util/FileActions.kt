package com.example.sweep.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.provider.MediaStore
import android.widget.Toast
import android.net.Uri
import android.content.ContentValues
import android.util.Log
import com.example.sweep.data.FileItem

/**
 * Opens Android's share sheet for [file], so the user can send it to
 * another app (messages, email, cloud storage, etc.).
 *
 * The receiving app is given temporary read access to this one file through
 * FLAG_GRANT_READ_URI_PERMISSION. That is why no storage permission is
 * needed to share, and why the receiver can't read anything else.
 */
fun shareFile(context: Context, file: FileItem) {
    val mimeType = context.contentResolver.getType(file.uri) ?: "*/*"

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, file.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(Intent.createChooser(sendIntent, "Share ${file.name}"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to share this file.", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Builds the system request to permanently delet file
 *
 * Launching the returned IntentSender show's Android's own configuration
 * dialog. File is deleted only if user allows. Android will delete the file itself
 */
// TODO: this deletes permanently, which is what a cleanup tool needs to free
// space. Consider MediaStore.createTrashRequest for a recoverable delete.
// TODO: createDeleteRequest acceps a list of Uris. Add multi-select so
// a whole duplicate group can be removed with a single confirm
fun createDeleteIntentSender(context: Context, file: FileItem): IntentSender {
    return MediaStore.createDeleteRequest(context.contentResolver, listOf(file.uri)).intentSender
}

/**
 * Open uri in another app that can display it
 * Shows a toast if no installed app handles the file type
 */
// TODO: BrowseScreen has its own private copy of this logic
// switch it to this shared, reuseable function, delete duplicate
fun openFile(context: Context, uri: Uri) {
    val mimeType = context.contentResolver.getType(uri) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file.", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Builds the system request asking the user to allow this app to modify
 * file. Launching the returned IntentSender shows Android's own
 * confirmation dialog; only after the user allows it can rename succeed.
 */
fun createWriteIntentSender(context: Context, file: FileItem): IntentSender {
    return MediaStore.createWriteRequest(context.contentResolver, listOf(file.uri)).intentSender
}

/**
 * Renames by updating its display name in MediaStore.
 * Call this only after the write request from createWireIntentSender was
 * approved. Returns false if the rename failed (for example, a file with
 * that name already exists in the same folder).
 *
 * MediaStore listeners are notified automatically, so every list that reads
 * from MediaStore updates on its own.
 */
// TODO: Rename and delete only work for MediaStore Uris. Files browsed through
// SAF (Browse tab) need DocumentsContract/DocumentFile equivalents.
fun renameFile(context: Context, file: FileItem, newName: String): Boolean {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
    }
    return try {
        context.contentResolver.update(file.uri, values, null, null) > 0
    } catch (e: Exception) {
        // A name clash and a lost permission both surface as exceptions here.
        Log.w("FileActions", "Rename failed for ${file.uri}", e)
        false
    }
}