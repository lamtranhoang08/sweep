package com.example.sweep.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
    private val fileSystemDataSource: FileSystemDataSource,
    private val fileHasher: FileHasher
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

    override fun getStorageSummary(): Flow<Map<StorageCategory, Long>> {
        return mediaStoreDataSource.getAllMedia().map { items ->
            items.groupBy { it.category }.mapValues { (_, categoryItems) ->
                categoryItems.sumOf {
                    it.sizeBytes
                }
            }
        }
    }

    // TODO: Sorting happens in memory after loading every media row.
    // At this app's scale that is fine, but MediaStore can sort and limit in the qurey itself
    // order by size desc Limit n, which would not avoid loading everything.
    override fun getLargeFiles(limit: Int): Flow<List<FileItem>> {
        return mediaStoreDataSource.getAllMedia().map { items ->
            items.sortedByDescending { it.sizeBytes }.take(limit)
        }
    }

    // flowOn goes AFTER map on purpose: operators placed after an upstream
    // flowOn run on the collector's thread (the main thread, for a
    // ViewModel), so without it the hashing would block the UI.
    override fun getDuplicateGroups(): Flow<List<DuplicateGroup>> {
        return mediaStoreDataSource.getAllMedia()
            .map { items -> findDuplicates(items) }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun findDuplicates(items: List<FileItem>): List<DuplicateGroup> {
        // Phase 1: files of different sizes can't be identical, so only
        // keep sizes shared by 2+ files. Empty files are ignored.
        val sameSizeGroups = items
            .filter { !it.isDirectory && it.sizeBytes > 0 }
            .groupBy { it.sizeBytes }
            .values
            .filter { it.size > 1 }

        // Phase 2: hash only those candidates and group by content.
        val duplicates = mutableListOf<DuplicateGroup>()
        for (candidates in sameSizeGroups) {
            val byHash = mutableMapOf<String, MutableList<FileItem>>()
            for (file in candidates) {
                // Stop promptly if the screen went away while hashing.
                currentCoroutineContext().ensureActive()
                val hash = fileHasher.sha256(file.uri) ?: continue
                byHash.getOrPut(hash) { mutableListOf() }.add(file)
            }
            for ((hash, files) in byHash) {
                if (files.size > 1) duplicates.add(DuplicateGroup(hash, files))
            }
        }

        return duplicates.sortedByDescending { it.reclaimableBytes }
    }
}