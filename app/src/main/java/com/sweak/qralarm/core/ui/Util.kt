package com.sweak.qralarm.core.ui

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
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

fun getDaysHoursAndMinutesUntilAlarm(alarmTimeInMillis: Long): Triple<Int, Int, Int> {
    val currentTimeInMillis = ZonedDateTime.now().toInstant().toEpochMilli()
    val diffDateTime = Instant
        .ofEpochMilli(alarmTimeInMillis - currentTimeInMillis)
        .atZone(ZoneId.of("UTC+00:00"))

    val days = (diffDateTime.dayOfYear - 1).run { if (this > 7) 0 else this }
    val hours = diffDateTime.hour
    val minutes = diffDateTime.minute

    return if (days == 0 && hours == 0 && minutes == 0) {
        Triple(0, 0, 1)
    } else {
        Triple(days, hours, minutes)
    }
}