package com.example.sweep

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Contract for fetching files and folders, independent of where the data
 * actually comes from (MediaStore, the Storage Access Framework, etc.).
 *
 * ViewModels depend on this interface only — never on a concrete
 * implementation — so the underlying data source can change, or be
 * swapped for a fake/test version, without any changes above this layer.
 * This is the "dependency inversion" principle in practice.
 */
interface FileRepository {

    /**
     * Returns a stream of files/folders located inside [folderUri].
     *
     * @param folderUri The folder to list contents of. Pass null to
     *   request the app's root/starting location.
     * @return A [Flow] emitting the current list of [`FileItem.kt`]s. A Flow
     *   is used instead of a plain List because scanning files is I/O
     *   work that can take time — a Flow lets results be delivered
     *   asynchronously without blocking the UI thread.
     */
    fun getFiles(folderUri: Uri? = null): Flow<List<FileItem>>

    /**
     * Returns total bytes used per StorageCategory, derived from all indexed media.
     * Used by the Storage screen's breakdown view.
     */
    fun getStorageSummary(): Flow<Map<StorageCategory, Long>>

}