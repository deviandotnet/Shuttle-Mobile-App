package com.example.shuttleapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun displayTime(): String {
    val timeMilli: Long = System.currentTimeMillis()
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(timeMilli))
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

fun displayMonthYear() : String {
    val dateMilli: Long =  System.currentTimeMillis()
    val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return format.format(Date(dateMilli))
}