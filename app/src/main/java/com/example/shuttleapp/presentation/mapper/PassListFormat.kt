package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.presentation.state.PassDaySection
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Search and day grouping for the Passes tab. Pure, so they're unit tested with a fixed clock. */
object PassListFormat {

    /** Case-insensitive match on the pass code or its "route · trip · time" line; a blank query matches everything. */
    fun matches(item: ShuttlePassItemUi, query: String): Boolean {
        val needle = query.trim()
        if (needle.isEmpty()) return true
        return item.code.contains(needle, ignoreCase = true) || item.summary.contains(needle, ignoreCase = true)
    }

    /**
     * Groups [items] (already newest first) by the day they were created, keeping that order:
     * "Today · Tue, Oct 20", "Yesterday · Mon, Oct 19", "Friday · Oct 16" within the week, then "Oct 9, 2026 · Fri".
     */
    fun groupByDay(
        items: List<ShuttlePassItemUi>,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): List<PassDaySection> =
        items.groupBy { format("yyyy-MM-dd", it.createdAt, timeZone, Locale.ROOT) }
            .map { (key, passes) ->
                val (title, dateLabel) = dayLabels(passes.first().createdAt, nowMillis, timeZone, locale)
                PassDaySection(key = key, title = title, dateLabel = dateLabel, passes = passes)
            }

    private fun dayLabels(millis: Long, nowMillis: Long, timeZone: TimeZone, locale: Locale): Pair<String, String> =
        when (DashboardFormat.daysBetween(millis, nowMillis, timeZone)) {
            0 -> "Today" to format("EEE, MMM d", millis, timeZone, locale)
            1 -> "Yesterday" to format("EEE, MMM d", millis, timeZone, locale)
            in 2..6 -> format("EEEE", millis, timeZone, locale) to format("MMM d", millis, timeZone, locale)
            else -> format("MMM d, yyyy", millis, timeZone, locale) to format("EEE", millis, timeZone, locale)
        }

    private fun format(pattern: String, millis: Long, timeZone: TimeZone, locale: Locale): String =
        SimpleDateFormat(pattern, locale).apply { this.timeZone = timeZone }.format(Date(millis))
}
