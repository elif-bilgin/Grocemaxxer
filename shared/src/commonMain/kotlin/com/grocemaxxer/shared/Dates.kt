package com.grocemaxxer.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Today's date as an ISO string (yyyy-MM-dd), used as the storage key. */
fun todayIsoDate(): String = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

/** "2026-07-18" -> "Sat, Jul 18" (falls back to the raw string on bad input). */
fun shortDateLabel(isoDate: String): String {
    val date = parseOrNull(isoDate) ?: return isoDate
    return "${date.dayOfWeek.shortName()}, ${date.month.shortName()} ${date.dayOfMonth}"
}

/** "2026-07-18" -> "Saturday, Jul 18" (falls back to the raw string on bad input). */
fun fullDateLabel(isoDate: String): String {
    val date = parseOrNull(isoDate) ?: return isoDate
    return "${date.dayOfWeek.longName()}, ${date.month.shortName()} ${date.dayOfMonth}"
}

/** "2026-07-18" -> "Sat, Jul 18, 2026" -- used in the date picker wheel. */
fun pickerDateLabel(isoDate: String): String {
    val date = parseOrNull(isoDate) ?: return isoDate
    return "${date.dayOfWeek.shortName()}, ${date.month.shortName()} ${date.dayOfMonth}, ${date.year}"
}

private fun parseOrNull(isoDate: String): LocalDate? =
    try {
        LocalDate.parse(isoDate)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun kotlinx.datetime.DayOfWeek.shortName(): String = longName().take(3)

private fun kotlinx.datetime.DayOfWeek.longName(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }

private fun kotlinx.datetime.Month.shortName(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
