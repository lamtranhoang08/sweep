package com.example.sweep.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.data.DuplicateGroup
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Holds UI state for the Cleanup screen: the largest files and the groups
 * of duplicate files. Both are read-only for now (deleting comes with the
 * file actions).
 *
 * Each list is wrapped in a [LoadState], starting at Loading, so the UI can
 * show a spinner while the work runs instead of a misleading "nothing found".
 */
class CleanupViewModel(
    repository: FileRepository
) : ViewModel() {

    val largeFiles: StateFlow<LoadState<List<FileItem>>> =
        repository.getLargeFiles()
            .map<List<FileItem>, LoadState<List<FileItem>>> { LoadState.Success(it) }
            .catch { emit(LoadState.Error(it.message ?: "Couldn't load large files")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = LoadState.Loading
            )

    // TODO: This re-hashes everything whenever the screen is left for more than
    // 5 seconds and reopened. Cache the result (or add a manual refresh) so
    // revisiting the tab doesn't repeat the expensive scan.
    val duplicateGroups: StateFlow<LoadState<List<DuplicateGroup>>> =
        repository.getDuplicateGroups()
            .map<List<DuplicateGroup>, LoadState<List<DuplicateGroup>>> { LoadState.Success(it) }
            .catch { emit(LoadState.Error(it.message ?: "Couldn't scan for duplicates")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = LoadState.Loading
            )

    /**
     * Builds [CleanupViewModel] with its [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CleanupViewModel(repository) as T
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}