package com.asef.dordambdandroid.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone

fun relativeTime(isoDate: String): String {
    return try {
        val then = Instant.parse(isoDate)
        val seconds = ChronoUnit.SECONDS.between(then, Instant.now())
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365
        when {
            years > 0 -> if (years == 1L) "1 year ago" else "$years years ago"
            months > 0 -> if (months == 1L) "1 month ago" else "$months months ago"
            weeks > 0 -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
            days > 0 -> if (days == 1L) "1 day ago" else "$days days ago"
            hours > 0 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            minutes > 0 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            else -> "just now"
        }
    } catch (e: Exception) {
        ""
    }
}

fun formatDateTime(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate) ?: return ""
        val outputFormat = SimpleDateFormat("hh:mm a - dd/MM/yyyy", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

fun Float.formatPrice(): String =
    if (this % 1 == 0f) this.toInt().toString() else "%.1f".format(this)
