package com.example.sweep

/**
 * Broad category a file belongs to, used for grouping storage totals
 * on the Storage screen. [OTHER] covers files from the file system (SAF)
 * that aren't classified by a specific media type.
 */
enum class StorageCategory {
    IMAGE,
    VIDEO,
    AUDIO,
    OTHER
}