package com.example.sweep

import android.net.Uri

/**
 * Plain data model representing a single file or folder in the app.
 *
 * This is the "Model" layer in MVVM — it has no dependency on Android UI
 * (Compose) or on any particular data source (MediaStore, SAF, etc.).
 * Every screen in the app (Browse, Gallery, Storage, Cleanup) works with
 * this same shape, regardless of where the data actually came from.
 *
 * @property name Display name of the file or folder, e.g. "vacation.jpg".
 * @property uri Content or document URI used to actually access the file.
 *   A Uri is used instead of a raw file path because scoped storage on
 *   modern Android often only exposes content:// URIs, not direct paths.
 * @property sizeBytes Size of the file in bytes. Folders may report 0 here.
 * @property isDirectory True if this item is a folder (navigate into it)
 *   rather than a file (open/preview it).
 * @property dateModified Last modified time, in epoch milliseconds.
 */
data class FileItem(
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val dateModified: Long
)