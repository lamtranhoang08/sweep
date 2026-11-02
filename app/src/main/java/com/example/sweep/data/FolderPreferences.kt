package com.example.sweep.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.IOException

// One DataStore per process; this delegate must live at the top level.
private val Context.sweepDataStore: DataStore<Preferences> by preferencesDataStore(name = "sweep_prefs")

/**
 * Remembers which folder the user picked in Browse, so the app can reopen it
 * after a restart.
 *
 * Android only keeps access to a picked folder while the app holds a
 * persisted permission for it, so restore checks that the permission still
 * exists and forgets the folder if it doesn't.
 */
// TODO: Picking a new folder doesn't release the old persisted permission, and
// Android caps how many an app may hold. Call releasePersistableUriPermission
// for the folder being replaced.
class FolderPreferences(context: Context) {

    private val appContext = context.applicationContext
    private val pickedFolderKey = stringPreferencesKey("picked_folder_uri")

    suspend fun save(uri: Uri) {
        try {
            appContext.sweepDataStore.edit { it[pickedFolderKey] = uri.toString() }
        } catch (e: IOException) {
            // Not being able to remember the folder is an inconvenience, not a crash.
        }
    }

    suspend fun clear() {
        try {
            appContext.sweepDataStore.edit { it.remove(pickedFolderKey) }
        } catch (e: IOException) {
            // See save()
        }
    }

    /**
     * Returns the remembered folder, or null if none was saved or Android no
     * longer grants access to it.
     */
    suspend fun restore(): Uri? {
        val saved: String? = try {
            appContext.sweepDataStore.data.first()[pickedFolderKey]
        } catch (e: IOException) {
            null
        }
        if (saved == null) return null

        val uri = Uri.parse(saved)
        val stillGranted = appContext.contentResolver.persistedUriPermissions
            .any { it.uri == uri && it.isReadPermission }

        if (!stillGranted) {
            clear()
            return null
        }
        return uri
    }
}