package com.example.shuttleapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/** The stored time format ("HH:mm") for departure, arrival and timeIn; [date] defaults to now. */
fun displayTime(date: Date = Date()): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

fun formatTimeHHmm(timeStr: String): String {
    if (timeStr.isEmpty()) return timeStr
    val parts = timeStr.split(":")
    if (parts.size >= 2) {
        return "${parts[0]}:${parts[1]}"
    }
    return timeStr
}

fun displayDate() : String {
    val dateMilli: Long = System.currentTimeMillis()
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date(dateMilli))
}

fun dateTimeCreated() : Long {
    val dateNano: Long = System.nanoTime()
    //val format = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
    val dateMilli = dateNano / 1_000_000 // convert nanoseconds to milliseconds
    return dateMilli
}

/**
 * Parses an ISO-8601 UTC/offset timestamp from the ASP.NET Core API (e.g. "2025-01-02T10:00:00Z",
 * "2025-01-02T10:00:00.1234567+08:00") to epoch millis. Fractional seconds are ignored. Null if unparseable.
 */
fun parseIsoUtcMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    val normalized = value.trim()
        .replace(Regex("\\.\\d+"), "")
        .replace(Regex("Z$"), "+00:00")
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(normalized)?.time
    } catch (ex: java.text.ParseException) {
        null
    }
}

fun displayMonthYear() : String = formatMonthYear(System.currentTimeMillis())

fun formatMonthYear(dateMilli: Long) : String {
    val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return format.format(Date(dateMilli))
}
/** "2026-10-20" (the pass date as stored) to "Tue, Oct 20, 2026". Returns the input unchanged if it isn't that format. */
fun formatPassDate(isoDate: String): String {
    if (isoDate.isBlank()) return isoDate
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate) ?: return isoDate
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(date)
    } catch (ex: java.text.ParseException) {
        isoDate
    }
}
