package com.example.sweep.util

import java.util.Locale

/**
 * Converts a raw byte count into a human-readable string (e.g. "2.30 MB").
 * Shared by every screen that shows file sizes.
 */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0

    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }

    return String.format(Locale.getDefault(), "%.2f %s", value, units[unitIndex])
}