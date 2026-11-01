package com.example.sweep.data

/**
 * Filters [items] down to the files that match [query] and, if given, [category].
 *
 * Matching is case-insensitive, and every word of the query must appear
 * somewhere in the file name, in any order.
 * A blank query returns an empty list on purpose: the Search screen shows a
 * hint rather than dumping the whole library.
 *
 * This is a plain function with no Android dependencies, so the matching
 * rules live in one place and can be swapped without touching any screen.
 */
// TODO: If fsearch has a ranking or fuzzy-matching approach worth porting, it
// can replace this body without changing any caller.
fun searchFiles(
    items: List<FileItem>,
    query: String,
    category: StorageCategory? = null
): List<FileItem> {
    val words = query.trim().lowercase().split(WHITESPACE).filter { it.isNotEmpty() }
    if (words.isEmpty()) return emptyList()

    val matches = items.filter { item ->
        if (category != null && item.category != category) return@filter false
        val name = item.name.lowercase()
        words.all { word -> name.contains(word) }
    }

    // Stable partition: prefix matches first, original order kept inside each group.
    val firstWord = words.first()
    val (prefixMatches, others) = matches.partition { it.name.lowercase().startsWith(firstWord) }
    return prefixMatches + others
}

private val WHITESPACE = Regex("\\s+")