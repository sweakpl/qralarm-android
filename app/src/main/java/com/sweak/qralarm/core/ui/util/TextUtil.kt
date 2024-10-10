package com.sweak.qralarm.core.ui.util

import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun DayOfWeek.shortName(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

fun getTimeString(hourOfDay: Int, minute: Int, is24HourFormat: Boolean): String {
    return ZonedDateTime.now()
        .withHour(hourOfDay)
        .withMinute(minute)
        .format(DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "hh:mm a"))
}