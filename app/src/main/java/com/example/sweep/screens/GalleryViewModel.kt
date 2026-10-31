package com.example.sweep.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepository
import com.example.sweep.data.StorageCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Holds UI state for the Gallery screen: the images and videos to show
 * in the thumbnail grid.
 *
 * Reuses [FileRepository.getFiles] with no folder (which returns all
 * indexed media) and filters out audio, since audio files have no
 * visual thumbnail. The filtering lives here rather than in the
 * repository because "what the Gallery shows" is a UI decision.
 */
class GalleryViewModel(
    repository: FileRepository
) : ViewModel() {
    // TODO: initialValue is emptyList(), so the screen can briefly flash
    //  "No media found" while loading. Wrap in LoadState (like Cleanup) to
    //  tell "loading" apart from "empty".
    val mediaItems: StateFlow<List<FileItem>> =
        repository.getFiles()
            .map { items ->
                items.filter {
                    it.category == StorageCategory.IMAGE ||
                            it.category == StorageCategory.VIDEO
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    /**
     * Builds [GalleryViewModel] with its [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(repository) as T
        }
    }

    private companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}