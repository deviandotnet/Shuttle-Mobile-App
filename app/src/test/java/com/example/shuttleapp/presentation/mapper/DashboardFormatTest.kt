package com.example.shuttleapp.presentation.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DashboardFormatTest {

    private val manila = TimeZone.getTimeZone("Asia/Manila")
    private val us = Locale.US

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(manila).apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }.timeInMillis

    private val now = at(2026, 10, 20, 13, 31)

    @Test
    fun `last synced says today, yesterday or the date`() {
        assertEquals("Last synced today, 10:42", DashboardFormat.lastSynced(at(2026, 10, 20, 10, 42), now, manila, us))
        assertEquals("Last synced today, 00:05", DashboardFormat.lastSynced(at(2026, 10, 20, 0, 5), now, manila, us))
        assertEquals("Last synced yesterday, 23:59", DashboardFormat.lastSynced(at(2026, 10, 19, 23, 59), now, manila, us))
        assertEquals("Last synced Oct 18, 07:40", DashboardFormat.lastSynced(at(2026, 10, 18, 7, 40), now, manila, us))
    }

    @Test
    fun `last synced handles a year boundary and no sync yet`() {
        val newYear = at(2027, 1, 1, 8, 0)
        assertEquals("Last synced yesterday, 18:00", DashboardFormat.lastSynced(at(2026, 12, 31, 18, 0), newYear, manila, us))
        assertEquals("Not synced from this device yet", DashboardFormat.lastSynced(null, now, manila, us))
    }

    @Test
    fun `header subtitle shows the date and the pass count for today`() {
        assertEquals("Tue, Oct 20 · 2 passes today", DashboardFormat.headerSubtitle(now, 2, manila, us))
        assertEquals("Tue, Oct 20 · 1 pass today", DashboardFormat.headerSubtitle(now, 1, manila, us))
        assertEquals("Tue, Oct 20 · No passes yet today", DashboardFormat.headerSubtitle(now, 0, manila, us))
    }

    @Test
    fun `counts only passes created today`() {
        val created = listOf(at(2026, 10, 20, 7, 45), at(2026, 10, 20, 0, 0), at(2026, 10, 19, 23, 59), at(2025, 10, 20, 9, 0))
        assertEquals(2, DashboardFormat.countCreatedToday(created, now, manila))
    }

    @Test
    fun `pending copy is singular and plural`() {
        assertEquals("1 pass waiting to sync", DashboardFormat.pendingCallout(1))
        assertEquals("3 passes waiting to sync", DashboardFormat.pendingCallout(3))
        assertEquals("All passes synced", DashboardFormat.pendingShort(0))
        assertEquals("1 pass waiting", DashboardFormat.pendingShort(1))
        assertEquals("3 passes waiting", DashboardFormat.pendingShort(3))
    }
}
