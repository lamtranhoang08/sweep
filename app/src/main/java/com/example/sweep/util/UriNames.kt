package com.example.sweep.util

import android.net.Uri
import android.provider.DocumentsContract

/**
 * Best-effort display name for a folder picked with the system picker, taken
 * from the document id inside the tree Uri ("primary:Music/Podcasts" becomes
 * "Podcasts"). Subfolders already carry their own name from the listing
 * this is only needed for the folder the user picked.
 */
fun folderNameFromTreeUri(uri: Uri): String {
    val documentId = try {
        DocumentsContract.getTreeDocumentId(uri)
    } catch (e: IllegalArgumentException) {
        return "Folder"
    }

    val name = documentId.substringAfterLast('/').substringAfterLast(':')
    // The root of a storage volume has an empty name after the colon.
    return name.ifEmpty { "Storage" }.replaceFirstChar { it.uppercase() }
}