package com.example.shuttleapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `stored names parse back to their mode`() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromStored(it.name)) }
    }

    @Test
    fun `missing or unknown values fall back to System`() {
        assertEquals(ThemeMode.System, ThemeMode.fromStored(null))
        assertEquals(ThemeMode.System, ThemeMode.fromStored(""))
        assertEquals(ThemeMode.System, ThemeMode.fromStored("dark"))
    }
}
