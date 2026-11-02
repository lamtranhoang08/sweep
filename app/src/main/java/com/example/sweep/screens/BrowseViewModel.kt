package com.example.sweep.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepository
import com.example.sweep.data.FolderPreferences
import com.example.sweep.util.folderNameFromTreeUri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

/** One step in the trail of folders the user has opened */
data class BrowseFolder(val uri: Uri, val name: String)

/**
 * Holds UI state for the Browse screen and coordinates with [FileRepository].
 *
 * The View ([BrowseScreen]) never touches [FileRepository] directly—it only
 * observes [files] and calls [openFolder]. This ViewModel mediates between
 * the UI and the data layer while surviving configuration changes like screen rotations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModel(
    repository: FileRepository,
    private val folderPreferences: FolderPreferences
) : ViewModel() {

    private val _folderStack = MutableStateFlow<List<BrowseFolder>>(emptyList())
    val folderStack: StateFlow<List<BrowseFolder>> = _folderStack.asStateFlow()

    /**
     * Contents of the current folder. Changing the trail cancels the old load flatMapLatest
     * and starts a new one.
     */
    val files: StateFlow<LoadState<List<FileItem>>> = _folderStack
        .flatMapLatest { stack ->
            val folder = stack.lastOrNull()
            repository.getFiles(folder?.uri)
                .map<List<FileItem>, LoadState<List<FileItem>>> { items ->
                    LoadState.Success(if (folder == null) items else items.sortedWith(FOLDERS_FIRST))
                }
                .onStart { emit(LoadState.Loading) }
                .catch { emit(LoadState.Error(it.message ?: "Couldn't load this folder")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = LoadState.Loading
        )

    // TODO: On launch the media view shows briefly before the saved folder is
    //  restored. Expose a "restoring" state so the screen can wait instead.
    init {
        viewModelScope.launch {
            val saved = folderPreferences.restore() ?: return@launch
            // Don't override a folder the user picked while this was loading.
            _folderStack.compareAndSet(
                emptyList(),
                listOf(BrowseFolder(saved, folderNameFromTreeUri(saved)))
            )
        }
    }

    /** User pick a folder with the system picker: start a fresh trail there */
    fun pickFolder(uri: Uri) {
        _folderStack.value = listOf(BrowseFolder(uri, folderNameFromTreeUri(uri)))
        viewModelScope.launch { folderPreferences.save(uri) }
    }

    /**
     * User pick a subfolder: go on level deeper
     */
    fun openFolder(uri: Uri) {
        _folderStack.update { stack ->
            stack + BrowseFolder(
                uri = uri,
                name = folderNameFromTreeUri(uri)
            )
        }
    }

    // TODO: Only the picked root is remembered. Persist the whole trail to
    //  reopen exactly where the user left off.
    /** Go up one level. Leaving the picked folder returns to the media view and forgets it. */
    fun navigateUp() {
        val current = _folderStack.value
        if (current.isEmpty()) return

        val remaining = current.dropLast(1)
        _folderStack.value = remaining
        if (remaining.isEmpty()) {
            viewModelScope.launch { folderPreferences.clear() }
        }
    }

    /**
     * Factory class to instantiate [BrowseViewModel] with its required [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository,
        private val folderPreferences: FolderPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BrowseViewModel(repository, folderPreferences) as T
        }
    }

    private companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        val FOLDERS_FIRST = compareByDescending<FileItem> { it.isDirectory }
            .thenBy { it.name.lowercase() }
    }
}