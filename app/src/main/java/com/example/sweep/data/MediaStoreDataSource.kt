package com.example.sweep.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Reads media files (images, videos, audio) using Android's MediaStore API.
 *
 * MediaStore is the OS-supported way to query media without needing broad
 * storage permissions — Android indexes these files itself. This class only
 * fetches data; it doesn't decide app policy (that's FileRepositoryImpl's job).
 */
class MediaStoreDataSource(private val context: Context) {

    /**
     * Streams all images, video, and audio the app can see, each tagged
     * with its [StorageCategory]. Emits once on collection,
     * then again every time MediaStorage reports a change, so the UI can
     * update.
     */
    fun getAllMedia(): Flow<List<FileItem>> = mediaChanges().conflate().map { loadAllMedia() }.flowOn(Dispatchers.IO)

    private fun mediaChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }

        val resolver = context.contentResolver
        COLLECTIONS.forEach { (uri, _) ->
            resolver.registerContentObserver(uri, true, observer)
        }
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    private fun loadAllMedia(): List<FileItem> = COLLECTIONS.flatMap { (uri, category) -> queryCollection(uri, category) }

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

    private companion object {
        /** Every collection we read, paired with the category its files get. */
        val COLLECTIONS = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI to StorageCategory.IMAGE,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI to StorageCategory.VIDEO,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to StorageCategory.AUDIO
        )
    }
}