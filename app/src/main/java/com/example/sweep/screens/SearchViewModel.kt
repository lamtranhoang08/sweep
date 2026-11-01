package com.example.sweep.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepository
import com.example.sweep.data.StorageCategory
import com.example.sweep.data.searchFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

/**
 * The files that matched a search, plus the exact query they were computed for.
 * Comparing query with what's currently typed tells the screen
 * whether these results are up to date or still watching on the debounce
 */
data class SearchResults(
    val query: String,
    val items: List<FileItem> = emptyList()
)

/**
 * Holds UI state for the Search screen: what the user typed, which type
 * filter is selected, and the files that match.
 *
 * The media library is collected once. Each keystroke re-filters that list in
 * memory instead of re-querying MediaStore, and because the library flow is
 * live, results also update if files change while the user is searching.
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    repository: FileRepository
) : ViewModel() {

    // The text field reads this directly, so typing shows up instantly.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // null means all types
    private val _category = MutableStateFlow<StorageCategory?>(null)
    val category: StateFlow<StorageCategory?> = _category.asStateFlow()

    // TODO: Add a LoadState for the first library load, so a very early search
    // can't flash "No files match" before the library has loaded.
    val results: StateFlow<SearchResults> = combine(
        repository.getFiles(),
        // Wait for a pause in typing before searching, but react instantly
        // when the field is cleared.
        _query.debounce { if (it.isBlank()) 0L else DEBOUNCE_MILLIS },
        _category
    ) { items, query, category ->
        SearchResults(query, searchFiles(items, query, category))
    }
        // Filtering is CPU work, so keep it off the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SearchResults(query = "")
        )

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun onCategorySelect(category: StorageCategory?) {
        _category.value = category
    }

    // TODO: Recent searches (needs persistent storage such as DataStore).

    /**
     * Builds [SearchViewModel] with its [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 250L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}