package com.example.sweep.data

import com.example.sweep.testFile
import org.junit.Assert.assertEquals
import org.junit.Test

class FileSearchTest {

    private val library = listOf(
        testFile("holiday-beach.jpg"),
        testFile("my-holiday.mp4", StorageCategory.VIDEO),
        testFile("IMG_001.jpg"),
        testFile("beach-song.mp3", StorageCategory.AUDIO),
        testFile("Holiday-Sunset.JPG")
    )

    // Comparing names keeps failures readable ("expected [a, b] but was [b]").
    private fun names(query: String, category: StorageCategory? = null): List<String> =
        searchFiles(library, query, category).map { it.name }

    @Test
    fun `blank query returns nothing`() {
        assertEquals(emptyList<String>(), names(""))
        assertEquals(emptyList<String>(), names("   "))
    }

    @Test
    fun `matching ignores case`() {
        assertEquals(listOf("IMG_001.jpg"), names("img"))
    }

    @Test
    fun `every word must match, in any order`() {
        assertEquals(listOf("holiday-beach.jpg"), names("beach holiday"))
    }

    @Test
    fun `extra whitespace in the query is ignored`() {
        assertEquals(listOf("holiday-beach.jpg"), names("  beach    holiday  "))
    }

    @Test
    fun `names starting with the first word come first`() {
        assertEquals(
            listOf("holiday-beach.jpg", "Holiday-Sunset.JPG", "my-holiday.mp4"),
            names("holiday")
        )
    }

    @Test
    fun `category filter narrows the results`() {
        assertEquals(listOf("my-holiday.mp4"), names("holiday", StorageCategory.VIDEO))
    }

    @Test
    fun `no match returns an empty list`() {
        assertEquals(emptyList<String>(), names("zzz"))
    }
}