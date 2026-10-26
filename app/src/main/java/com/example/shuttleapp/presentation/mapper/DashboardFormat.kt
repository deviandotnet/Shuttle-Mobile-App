package com.example.shuttleapp.presentation.mapper

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Display strings for Home, the drawer and the Sync tab. Pure, so they're unit tested with a fixed clock. */
object DashboardFormat {

    /** "Last synced today, 10:42", "Last synced yesterday, 18:05", "Last synced Oct 18, 07:40". */
    fun lastSynced(
        atMillis: Long?,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        if (atMillis == null) return "Not synced from this device yet"
        val time = format("HH:mm", atMillis, timeZone, locale)
        val day = when (daysBetween(atMillis, nowMillis, timeZone)) {
            0 -> "today"
            1 -> "yesterday"
            else -> format("MMM d", atMillis, timeZone, locale)
        }
        return "Last synced $day, $time"
    }

    /** "Tue, Oct 20 · 2 passes today". */
    fun headerSubtitle(
        nowMillis: Long,
        passesToday: Int,
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val count = when (passesToday) {
            0 -> "No passes yet today"
            1 -> "1 pass today"
            else -> "$passesToday passes today"
        }
        return "${format("EEE, MMM d", nowMillis, timeZone, locale)} · $count"
    }

    fun countCreatedToday(createdMillis: List<Long>, nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Int =
        createdMillis.count { daysBetween(it, nowMillis, timeZone) == 0 }

    /** "3 passes waiting to sync". */
    fun pendingCallout(count: Int): String = if (count == 1) "1 pass waiting to sync" else "$count passes waiting to sync"

    /** "3 passes waiting", or "All passes synced" when there is nothing to upload. */
    fun pendingShort(count: Int): String = when (count) {
        0 -> "All passes synced"
        1 -> "1 pass waiting"
        else -> "$count passes waiting"
    }

    private fun format(pattern: String, millis: Long, timeZone: TimeZone, locale: Locale): String =
        SimpleDateFormat(pattern, locale).apply { this.timeZone = timeZone }.format(Date(millis))

    /** Calendar days from [fromMillis] to [toMillis] in [timeZone]; 0 means the same day. Rounds away DST shifts. */
    internal fun daysBetween(fromMillis: Long, toMillis: Long, timeZone: TimeZone): Int {
        fun startOfDay(millis: Long): Long = Calendar.getInstance(timeZone).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return Math.round((startOfDay(toMillis) - startOfDay(fromMillis)).toDouble() / DAY_MILLIS).toInt()
    }

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
}
