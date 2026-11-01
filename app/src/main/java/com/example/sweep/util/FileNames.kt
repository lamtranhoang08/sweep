package com.example.sweep.util

/**
 * A file name split into the part users edit and the part they shouldn't.
 *
 * @property base The name without its extension, e.g. "holiday".
 * @property extension The extension including the dot, e.g. ".jpg", or an
 *   empty string if there isn't one.
 */
data class NameParts(val base: String, val extension: String)

/**
 * Splits [name] at its last dot. A leading dot (".nomedia") is treated as
 * part of the name, not as an extension.
 */
fun splitFileName(name: String): NameParts {
    val dot = name.lastIndexOf('.')
    return if (dot > 0) {
        NameParts(base = name.substring(0, dot), extension = name.substring(dot))
    } else {
        NameParts(base = name, extension = "")
    }
}