package com.example.shuttleapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PassFilterTest {

    @Test
    fun `route values map to filters and anything else shows all`() {
        assertEquals(PassFilter.Late, PassFilter.fromRoute("Late"))
        assertEquals(PassFilter.NotSynced, PassFilter.fromRoute("NotSynced"))
        assertEquals(PassFilter.All, PassFilter.fromRoute(null))
        assertEquals(PassFilter.All, PassFilter.fromRoute("late"))
        assertEquals(PassFilter.All, PassFilter.fromRoute(""))
    }
}
