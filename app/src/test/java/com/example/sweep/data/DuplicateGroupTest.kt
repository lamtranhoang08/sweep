package com.example.sweep.data

import com.example.sweep.testFile
import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicateGroupTest {

    @Test
    fun `two copies reclaim one copy's worth of space`() {
        val group = DuplicateGroup(
            hash = "abc",
            files = listOf(testFile("a.jpg", sizeBytes = 500), testFile("b.jpg", sizeBytes = 500))
        )
        assertEquals(500L, group.reclaimableBytes)
    }

    @Test
    fun `three copies reclaim two copies' worth of space`() {
        val group = DuplicateGroup(
            hash = "abc",
            files = listOf(
                testFile("a.jpg", sizeBytes = 500),
                testFile("b.jpg", sizeBytes = 500),
                testFile("c.jpg", sizeBytes = 500)
            )
        )
        assertEquals(1_000L, group.reclaimableBytes)
    }
}