package com.example.sweep.data

/**
 * A set of files with identical content (same SHA-256 hash)
 *
 * @property hash SHA-256 hash of the file
 * @property files Every file sharing that content. Always 2 or more
 */
data class DuplicateGroup (
    val hash: String,
    val files: List<FileItem>
) {
    /**
     * Space freed by keeping one copy and removing the rest. All files in a group
     * are the same size, so this is (copies - 1) * size
     */
    val reclaimableBytes: Long
        get() = (files.size - 1) * (files.firstOrNull()?.sizeBytes ?: 0L)
}