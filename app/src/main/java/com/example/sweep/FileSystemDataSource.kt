package com.example.sweep

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Browses files and folders inside a user-picked folder tree using
 * Storage Access Framework instead of legacy runtime permissions.
 *
 * The user grants access to one folder via the system document picker.
 * This class lists the contents of a given folder URI-it does not request permission itself
 */

class FileSystemDataSource(
    private val context: Context
) {
    /**
     * Lists the immediate children (files and subdirectories) inside [folderUri].
     *
     * @param folderUri A tree [Uri] the app already holds persistent access permissions for.
     * @return A [Flow] emitting the list of [FileItem] metadata mapped from SAF.
     */
    fun listFiles(folderUri: Uri): Flow<List<FileItem>> = flow {
        val rootDoc = DocumentFile.fromTreeUri(context, folderUri)
        val files = rootDoc?.listFiles()?.map { doc ->
            FileItem(
                name = doc.name ?: "Unknown",
                uri = doc.uri,
                sizeBytes = doc.length(),
                isDirectory = doc.isDirectory,
                dateModified = doc.lastModified()
            )
        }.orEmpty()

        emit(files)
    }.flowOn(Dispatchers.IO)
}