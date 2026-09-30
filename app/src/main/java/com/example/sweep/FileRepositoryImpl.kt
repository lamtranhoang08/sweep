package com.example.sweep

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Combines [MediaStoreDataSource] and [FileSystemDataSource] behind the single
 * [FileRepository] contract that ViewModels depend on.
 *
 * This class decides *which* data source answers a given request—that
 * decision lives here, not in the ViewModel or in the data sources
 * themselves, keeping each piece focused on one job.
 */
class FileRepositoryImpl(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val fileSystemDataSource: FileSystemDataSource
) : FileRepository {

    /**
     * Retrieves the list of files based on whether a directory URI is provided.
     *
     * @param folderUri Optional [Uri] representing a folder tree picked via SAF.
     * @return A [Flow] emitting the list of [FileItem]s from either MediaStore or FileSystem.
     */
    override fun getFiles(folderUri: Uri?): Flow<List<FileItem>> {
        return if (folderUri == null) {
            // No folder picked yet — show all indexed media as a default view.
            mediaStoreDataSource.getAllMedia()
        } else {
            // A specific folder was picked via SAF — browse its real contents.
            fileSystemDataSource.listFiles(folderUri)
        }
    }
}