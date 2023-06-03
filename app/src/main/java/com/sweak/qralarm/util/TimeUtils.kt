package com.sweak.qralarm.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun currentTimeInMillis(): Long =
    ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

fun getAlarmTimeInMillis(hourOfDay: Int, minute: Int): Long {
    var zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
    zonedDateTime = zonedDateTime.withHour(hourOfDay)
    zonedDateTime = zonedDateTime.withMinute(minute)
    zonedDateTime = zonedDateTime.withSecond(0)
    zonedDateTime = zonedDateTime.withNano(0)

    if (zonedDateTime.toInstant().toEpochMilli() <= currentTimeInMillis()) {
        zonedDateTime = zonedDateTime.plusDays(1)
    }

    return zonedDateTime.toInstant().toEpochMilli()
}

fun getSnoozeAlarmTimeInMillis(snoozeDurationInMinutes: Int): Long {
    var zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
    zonedDateTime = zonedDateTime.plusMinutes(snoozeDurationInMinutes.toLong())
    zonedDateTime = zonedDateTime.withSecond(0)
    zonedDateTime = zonedDateTime.withNano(0)

    if (zonedDateTime.toInstant().toEpochMilli() <= currentTimeInMillis()) {
        zonedDateTime = zonedDateTime.plusDays(1)
    }

    return zonedDateTime.toInstant().toEpochMilli()
}

fun getAlarmHourOfDay(alarmTimeInMillis: Long): Int =
    Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).hour

fun getAlarmMinute(alarmTimeInMillis: Long): Int =
    Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).minute

fun getHoursAndMinutesUntilTimePair(timeInMillis: Long): Pair<Int, Int> =
    getHoursAndMinutesUntilTimePairFromTime(currentTimeInMillis(), timeInMillis)

fun getHoursAndMinutesUntilTimePairFromTime(fromTime: Long, untilTime: Long): Pair<Int, Int> {
    val diffDateTime =
        Instant.ofEpochMilli(untilTime - fromTime).atZone(ZoneId.of("UTC+00:00"))

    val hours = diffDateTime.hour
    val minutes = diffDateTime.minute

    return if (hours == 0 && minutes == 0) {
        Pair(0, 1)
    } else {
        Pair(hours, minutes)
    }
}