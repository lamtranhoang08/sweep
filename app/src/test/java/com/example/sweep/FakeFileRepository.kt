package com.example.sweep

import android.net.Uri
import com.example.sweep.data.DuplicateGroup
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepository
import com.example.sweep.data.StorageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory FileRepository for tests.
 * Each piece of data can be set directly,
 * so a test controls exactly what the ViewModel sees.
 */
class FakeFileRepository(
    initialFiles: List<FileItem> = emptyList()
) : FileRepository {

    /** The library. Assign a new list to simulate files being added or removed. */
    val files = MutableStateFlow(initialFiles)

    /** When set, getFiles fails with this error instead of emitting the library. */
    var filesFailure: Throwable? = null

    var largeFilesFlow: Flow<List<FileItem>> = flowOf(emptyList())

    var duplicateGroupsFlow: Flow<List<DuplicateGroup>> = flowOf(emptyList())

    override fun getFiles(folderUri: Uri?): Flow<List<FileItem>> {
        val failure = filesFailure

        return if (failure != null) {
            flow { throw failure }
        } else {
            files
        }
    }

    override fun getStorageSummary(): Flow<Map<StorageCategory, Long>> =
        flowOf(emptyMap())

    override fun getLargeFiles(limit: Int): Flow<List<FileItem>> =
        largeFilesFlow

    override fun getDuplicateGroups(): Flow<List<DuplicateGroup>> =
        duplicateGroupsFlow
}