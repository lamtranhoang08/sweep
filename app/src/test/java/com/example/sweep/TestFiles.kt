package com.example.sweep

import android.net.Uri
import com.example.sweep.data.FileItem
import com.example.sweep.data.StorageCategory
import io.mockk.mockk

/**
 * Builds a [FileItem] for tests. Only the fields a test cares about need to
 * be given; the Uri is a stand-in because no test here reads it.
 */
fun testFile(
    name: String,
    category: StorageCategory = StorageCategory.IMAGE,
    sizeBytes: Long = 1_000L
): FileItem = FileItem(
    name = name,
    uri = mockk<Uri>(),
    sizeBytes = sizeBytes,
    isDirectory = false,
    dateModified = 0L,
    category = category
)