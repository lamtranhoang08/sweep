package com.example.sweep.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FileNamesTest {

    @Test
    fun `splits a simple name at the dot`() {
        assertEquals(NameParts("holiday", ".jpg"), splitFileName("holiday.jpg"))
    }

    @Test
    fun `splits at the last dot only`() {
        assertEquals(NameParts("archive.tar", ".gz"), splitFileName("archive.tar.gz"))
    }

    @Test
    fun `a name with no extension keeps an empty extension`() {
        assertEquals(NameParts("README", ""), splitFileName("README"))
    }

    @Test
    fun `a leading dot is part of the name, not an extension`() {
        assertEquals(NameParts(".nomedia", ""), splitFileName(".nomedia"))
    }
}