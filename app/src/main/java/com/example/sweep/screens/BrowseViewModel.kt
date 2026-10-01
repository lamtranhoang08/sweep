package com.example.sweep.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.FileItem
import com.example.sweep.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Holds UI state for the Browse screen and coordinates with [com.example.sweep.FileRepository].
 *
 * The View ([BrowseScreen]) never touches [com.example.sweep.FileRepository] directly—it only
 * observes [files] and calls [openFolder]. This ViewModel mediates between
 * the UI and the data layer while surviving configuration changes like screen rotations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModel(
    private val repository: FileRepository
) : ViewModel() {

    // Tracks which folder is currently open. null = default/root view.
    private val currentFolderUri = MutableStateFlow<Uri?>(null)

    /**
     * Files/folders to display, automatically re-fetched whenever [currentFolderUri] changes.
     * [flatMapLatest] cancels any in-flight fetch for the old folder before starting the new one.
     */
    val files: StateFlow<List<FileItem>> = currentFolderUri
        .flatMapLatest { uri ->
            repository.getFiles(uri)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    /**
     * Called when the user taps a folder row or picks a folder via SAF.
     *
     * @param uri The destination folder [Uri] to navigate into.
     */
    fun openFolder(uri: Uri) {
        currentFolderUri.value = uri
    }

    /**
     * Factory class to instantiate [BrowseViewModel] with its required [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BrowseViewModel(repository) as T
        }
    }

    private companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}