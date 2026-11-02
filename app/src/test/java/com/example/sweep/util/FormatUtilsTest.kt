package com.example.sweep.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class FormatUtilsTest {

    private lateinit var originalLocale: Locale

    // formatBytes uses the device's number format on purpose ("1,50" in some
    // countries), so the tests pin a locale to get the same output everywhere.
    @Before
    fun pinLocale() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `zero and negative sizes show as 0 B`() {
        assertEquals("0 B", formatBytes(0))
        assertEquals("0 B", formatBytes(-5))
    }

    @Test
    fun `sizes below one KB stay in bytes`() {
        assertEquals("512.00 B", formatBytes(512))
    }

    @Test
    fun `sizes scale through KB, MB and GB`() {
        assertEquals("1.00 KB", formatBytes(1024))
        assertEquals("1.50 KB", formatBytes(1536))
        assertEquals("1.00 MB", formatBytes(1024L * 1024))
        assertEquals("1.00 GB", formatBytes(1024L * 1024 * 1024))
    }

    @Test
    fun `sizes above a terabyte stay in TB`() {
        assertEquals("1024.00 TB", formatBytes(1L shl 50))
    }
}