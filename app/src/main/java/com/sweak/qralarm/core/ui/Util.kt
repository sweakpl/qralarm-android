package com.sweak.qralarm.core.ui

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
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

fun getTimeString(timeInMillis: Long, is24HourFormat: Boolean): String {
    return Instant.ofEpochMilli(timeInMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "hh:mm a"))
}

fun getDaysHoursAndMinutesUntilAlarm(alarmTimeInMillis: Long): Triple<Int, Int, Int> {
    val diffDateTime = Instant
        .ofEpochMilli(alarmTimeInMillis - System.currentTimeMillis())
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

fun convertAlarmRepeatingMode(
    repeatingMode: Alarm.RepeatingMode
): AlarmRepeatingScheduleWrapper? {
    if (repeatingMode is Alarm.RepeatingMode.Once) {
        return AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE)
    } else if (repeatingMode is Alarm.RepeatingMode.Days) {
        val days = repeatingMode.repeatingDaysOfWeek

        if (days.size == 2 && days.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))) {
            return AlarmRepeatingScheduleWrapper(alarmRepeatingMode = SAT_SUN)
        } else if (days.size == 5 &&
            days.containsAll(
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            )
        ) {
            return AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI)
        } else if (days.size == 7) {
            return AlarmRepeatingScheduleWrapper(alarmRepeatingMode = EVERYDAY)
        } else {
            return AlarmRepeatingScheduleWrapper(
                alarmRepeatingMode = CUSTOM,
                alarmDaysOfWeek = days
            )
        }
    }

    return null
}

fun getDayString(timeInMillis: Long): String {
    return Instant.ofEpochMilli(timeInMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE d.M"))
}