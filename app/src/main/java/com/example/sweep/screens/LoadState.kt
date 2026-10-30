package com.example.sweep.screens

/**
 * What a screen's data is doing right now. Using this instead of a bare
 * list lets the UI tell "still loading" apart from "loaded, but empty".
 *
 * [T] is declared 'out' (covariant) so Loading and Error, which carry no
 * data, can be used as a LoadState of any type.
 */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}