package com.sweak.qralarm.core.domain.alarm

import java.time.Instant
import java.time.ZoneId

fun getHourAndMinuteOfAlarmTimeInMillis(
    alarmTimeInMillis: Long,
    zoneId: ZoneId
): Pair<Int, Int> {
    val alarmDateTime = Instant
        .ofEpochMilli(alarmTimeInMillis)
        .atZone(zoneId)

    return Pair(alarmDateTime.hour, alarmDateTime.minute)
}
