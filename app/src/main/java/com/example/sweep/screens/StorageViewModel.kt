package com.example.sweep.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sweep.FileRepository
import com.example.sweep.StorageCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Holds UI state for the Storage screen—total bytes used per category.
 *
 * This screen is read-only (no navigation or user-triggered actions beyond viewing),
 * so the ViewModel serves as a reactive pass-through from [com.example.sweep.FileRepository] to the UI,
 * converted into a [kotlinx.coroutines.flow.StateFlow] that Compose can observe safely across lifecycle changes. */
class StorageViewModel(
    repository: FileRepository
) : ViewModel() {

    /**
     * Map of [StorageCategory] to total size in bytes, emmited as an observable
     */
    val storageSummary: StateFlow<Map<StorageCategory, Long>> =
        repository.getStorageSummary().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyMap()
        )

    /**
     * Factory class to instantiate [StorageViewModel] with its requireed [FileRepository] dependency.
     */
    class Factory(
        private val repository: FileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StorageViewModel(repository) as T
        }
    }

    private companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}