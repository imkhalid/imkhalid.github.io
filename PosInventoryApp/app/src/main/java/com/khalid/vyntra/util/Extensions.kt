package com.khalid.vyntra.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Date Formatting ────────────────────────────────────────────────────────────

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

/**
 * Formats a timestamp (epoch millis) to "dd MMM yyyy" (e.g. "26 May 2026").
 */
fun Long.toFormattedDate(): String {
    val instant = Instant.ofEpochMilli(this)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.format(dateFormatter)
}

/**
 * Formats a timestamp (epoch millis) to "dd MMM yyyy, hh:mm a" (e.g. "26 May 2026, 02:30 PM").
 */
fun Long.toFormattedDateTime(): String {
    val instant = Instant.ofEpochMilli(this)
    val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    return localDateTime.format(dateTimeFormatter)
}

// ── Currency Formatting ────────────────────────────────────────────────────────

/**
 * Formats a Double as currency with the given symbol (e.g. "Rs 1,234.56").
 */
fun Double.toCurrency(symbol: String = "Rs"): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$symbol ${formatter.format(this)}"
}

// ── Search ─────────────────────────────────────────────────────────────────────

/**
 * Prepares a string for search by trimming whitespace and converting to lowercase.
 */
fun String.toSearchQuery(): String = trim().lowercase(Locale.getDefault())

// ── Day Boundaries ─────────────────────────────────────────────────────────────

/**
 * Returns the epoch millis for the start of the day (00:00:00.000) containing the given [timestamp].
 */
fun startOfDay(timestamp: Long): Long {
    val localDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

/**
 * Returns the epoch millis for the end of the day (23:59:59.999) containing the given [timestamp].
 */
fun endOfDay(timestamp: Long): Long {
    val localDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.atTime(LocalTime.MAX)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

// ── Month Boundaries ───────────────────────────────────────────────────────────

/**
 * Returns the epoch millis for the start of the given month (1-based) in the given year.
 */
fun startOfMonth(year: Int, month: Int): Long {
    val localDate = LocalDate.of(year, month, 1)
    return localDate.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

/**
 * Returns the epoch millis for the end of the given month (1-based) in the given year (23:59:59.999 of the last day).
 */
fun endOfMonth(year: Int, month: Int): Long {
    val yearMonth = YearMonth.of(year, month)
    val lastDay = yearMonth.atEndOfMonth()
    return lastDay.atTime(LocalTime.MAX)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
