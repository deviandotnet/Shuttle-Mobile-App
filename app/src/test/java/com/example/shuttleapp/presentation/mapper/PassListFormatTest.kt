package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PassListFormatTest {

    private val manila = TimeZone.getTimeZone("Asia/Manila")
    private val us = Locale.US

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(manila).apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }.timeInMillis

    private val now = at(2026, 10, 20, 13, 34)

    private fun item(id: String, createdAt: Long, summary: String = "Villa Estrella · In · 07:45") = ShuttlePassItemUi(
        id = id, code = "SPT-2026-10-$id", schedule = "", routeAndTrip = "",
        isLate = false, isSynced = false, summary = summary, createdAt = createdAt
    )

    @Test
    fun `groups newest first into today, yesterday, weekday and dated sections`() {
        val items = listOf(
            item("0012", at(2026, 10, 20, 7, 45)),
            item("0011", at(2026, 10, 19, 17, 10)),
            item("0010", at(2026, 10, 19, 7, 40)),
            item("0009", at(2026, 10, 16, 17, 5)),
            item("0001", at(2026, 10, 9, 7, 30))
        )

        val sections = PassListFormat.groupByDay(items, now, manila, us)

        assertEquals(
            listOf(
                Triple("Today", "Tue, Oct 20", listOf("0012")),
                Triple("Yesterday", "Mon, Oct 19", listOf("0011", "0010")),
                Triple("Friday", "Oct 16", listOf("0009")),
                Triple("Oct 9, 2026", "Fri", listOf("0001"))
            ),
            sections.map { Triple(it.title, it.dateLabel, it.passes.map(ShuttlePassItemUi::id)) }
        )
        assertEquals(sections.size, sections.map { it.key }.toSet().size)
    }

    @Test
    fun `day boundaries follow the local time zone`() {
        val sections = PassListFormat.groupByDay(
            listOf(item("a", at(2026, 10, 20, 0, 5)), item("b", at(2026, 10, 19, 23, 59))),
            now, manila, us
        )
        assertEquals(listOf("Today", "Yesterday"), sections.map { it.title })
    }

    @Test
    fun `no passes means no sections`() {
        assertTrue(PassListFormat.groupByDay(emptyList(), now, manila, us).isEmpty())
    }

    @Test
    fun `search matches the code or the route line, ignoring case and spaces`() {
        val pass = item("0011", now, summary = "Mamatid · Out · 17:10")
        assertTrue(PassListFormat.matches(pass, ""))
        assertTrue(PassListFormat.matches(pass, "   "))
        assertTrue(PassListFormat.matches(pass, "spt-2026-10-0011"))
        assertTrue(PassListFormat.matches(pass, " mamatid "))
        assertTrue(PassListFormat.matches(pass, "17:10"))
        assertFalse(PassListFormat.matches(pass, "Villa"))
    }
}
