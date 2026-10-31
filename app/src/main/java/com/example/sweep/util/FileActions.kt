package com.example.sweep.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
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