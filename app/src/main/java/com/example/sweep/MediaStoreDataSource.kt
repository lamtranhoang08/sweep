package com.example.sweep

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Reads media files (images, videos, audio) using Android's MediaStore API.
 *
 * MediaStore is the OS-supported way to query media without needing broad
 * storage permissions — Android indexes these files itself. This class only
 * fetches data; it doesn't decide app policy (that's FileRepositoryImpl's job).
 */
class MediaStoreDataSource(private val context: Context) {

    /**
     * Queries all images, videos, and audio the app can see, combined into
     * a single list of FileItems. Runs on Dispatchers.IO since this is
     * disk/database work and must never block the UI thread.
     */
    fun getAllMedia(): Flow<List<FileItem>> = flow {
        val items = mutableListOf<FileItem>()
        items += queryCollection(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, StorageCategory.IMAGE)
        items += queryCollection(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, StorageCategory.VIDEO)
        items += queryCollection(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, StorageCategory.AUDIO)
        emit(items)
    }.flowOn(Dispatchers.IO)

    /**
     * Runs a single MediaStore query against [collectionUri] and maps
     * each row into a FileItem.
     */
    private fun queryCollection(
        collectionUri: Uri,
        category: StorageCategory
    ): List<FileItem> {
        val results = mutableListOf<FileItem>()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        context.contentResolver.query(
            collectionUri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val itemUri = ContentUris.withAppendedId(collectionUri, id)
                results += FileItem(
                    name = cursor.getString(nameCol) ?: "Unknown",
                    uri = itemUri,
                    sizeBytes = cursor.getLong(sizeCol),
                    isDirectory = false,
                    dateModified = cursor.getLong(dateCol) * 1000L, // MediaStore stores seconds, we use millis
                    category = category
                )
            }
        }

        return results
    }
}