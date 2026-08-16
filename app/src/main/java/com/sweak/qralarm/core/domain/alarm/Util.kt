package com.sweak.qralarm.core.domain.alarm

import java.time.Instant
import java.time.ZoneId

fun getHourAndMinuteOfAlarmTimeInMillis(alarmTimeInMillis: Long): Pair<Int, Int> {
    val alarmDateTime = Instant
        .ofEpochMilli(alarmTimeInMillis)
        .atZone(ZoneId.systemDefault())

    return Pair(alarmDateTime.hour, alarmDateTime.minute)
}
