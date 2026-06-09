package com.sweak.qralarm.core.ui

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
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
    val totalSeconds = (alarmTimeInMillis - System.currentTimeMillis()) / 1000
    val days = (totalSeconds / 86400).toInt()
    val hours = ((totalSeconds % 86400) / 3600).toInt()
    val minutes = ((totalSeconds % 3600) / 60).toInt()

    return if (days == 0 && hours == 0 && minutes == 0) {
        Triple(0, 0, 1)
    } else {
        Triple(days, hours, minutes)
    }
}

fun getHourAndMinuteOfAlarmTimeInMillis(alarmTimeInMillis: Long): Pair<Int, Int> {
    val alarmDateTime = Instant
        .ofEpochMilli(alarmTimeInMillis)
        .atZone(ZoneId.systemDefault())

    return Pair(alarmDateTime.hour, alarmDateTime.minute)
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
    val datePattern = (DateFormat.getDateInstance(
        DateFormat.SHORT,
        Locale.getDefault()
    ) as SimpleDateFormat).toPattern()
    val datePatternWithoutYear = datePattern
        .replace(
            regex = Regex(pattern = "[yY]+"),
            replacement = ""
        )
        .trim { it !in 'A'..'z' }

    val dateTime = Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault())
    val differentYear = dateTime.year != ZonedDateTime.now().year
    val pattern = if (differentYear) "EEEE $datePattern" else "EEEE $datePatternWithoutYear"

    return dateTime.format(DateTimeFormatter.ofPattern(pattern))
}

fun getEarliestOnlyOnceAlarmDate(hourOfDay: Int, minute: Int): LocalDate {
    val now = ZonedDateTime.now()
    val todayAtAlarmTime = now.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0)
    return if (todayAtAlarmTime > now) now.toLocalDate() else now.toLocalDate().plusDays(1)
}