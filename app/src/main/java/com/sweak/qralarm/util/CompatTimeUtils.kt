package com.sweak.qralarm.util

import android.os.Build
import com.sweak.qralarm.ui.screens.home.AlarmTimeUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

fun currentTimeInMillis(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } else {
        Calendar.getInstance(TimeZone.getDefault()).timeInMillis
    }
}

fun currentTimeInMinutes(): Int
{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
            .minute
    } else {
        Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE)
    }
}

fun getAlarmTimeInMillis(time: AlarmTimeUiState): Long
{
    return getAlarmTimeInMillis(time.hour, time.minute, time.timeFormat, time.meridiem)
}

fun getAlarmTimeInMillis(hour: Int, minute: Int, timeFormat: TimeFormat, meridiem: Meridiem): Long {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
        zonedDateTime = zonedDateTime.withHour(
            if (timeFormat == TimeFormat.MILITARY) {
                hour
            } else {
                if (meridiem == Meridiem.AM) {
                    if (hour == 12) 0 else hour
                } else {
                    if (hour == 12) 12 else hour + 12
                }
            }
        )
        zonedDateTime = zonedDateTime.withMinute(minute)
        zonedDateTime = zonedDateTime.withSecond(0)
        zonedDateTime = zonedDateTime.withNano(0)

        if (zonedDateTime.toInstant().toEpochMilli() <= currentTimeInMillis()) {
            zonedDateTime = zonedDateTime.plusDays(1)
        }

        return zonedDateTime.toInstant().toEpochMilli()
    } else {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(
                if (timeFormat == TimeFormat.MILITARY) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                when {
                    timeFormat == TimeFormat.MILITARY -> hour
                    hour == 12 -> 0
                    else -> hour
                }
            )
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeFormat == TimeFormat.AMPM) {
                set(
                    Calendar.AM_PM,
                    if (meridiem == Meridiem.AM) Calendar.AM else Calendar.PM
                )
            }
        }

        if (calendar.timeInMillis <= currentTimeInMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }

        return calendar.timeInMillis
    }
}

fun getSnoozeAlarmTimeInMillis(snoozeDurationInMinutes: Int): Long {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())
        zonedDateTime = zonedDateTime.plusMinutes(snoozeDurationInMinutes.toLong())
        zonedDateTime = zonedDateTime.withSecond(0)
        zonedDateTime = zonedDateTime.withNano(0)

        if (zonedDateTime.toInstant().toEpochMilli() <= currentTimeInMillis()) {
            zonedDateTime = zonedDateTime.plusDays(1)
        }

        return zonedDateTime.toInstant().toEpochMilli()
    } else {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            add(Calendar.MINUTE, snoozeDurationInMinutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= currentTimeInMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }

        return calendar.timeInMillis
    }
}

fun getAlarmHour(alarmTimeInMillis: Long, timeFormat: TimeFormat): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val alarmZonedDateTime =
            Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault())

        if (timeFormat == TimeFormat.MILITARY) {
            return alarmZonedDateTime.hour
        } else {
            alarmZonedDateTime.hour.apply {
                return when {
                    this == 0 -> 12
                    this <= 12 -> this
                    else -> this - 12
                }
            }
        }
    } else {
        val alarmCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = alarmTimeInMillis
        }

        if (timeFormat == TimeFormat.MILITARY) {
            return alarmCalendar.get(Calendar.HOUR_OF_DAY)
        } else {
            alarmCalendar.get(Calendar.HOUR).apply {
                return if (this == 0) 12 else this
            }
        }
    }
}

fun getAlarmMinute(alarmTimeInMillis: Long): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).minute
    } else {
        Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = alarmTimeInMillis
            return get(Calendar.MINUTE)
        }
    }
}

fun getAlarmMeridiem(alarmTimeInMillis: Long): Meridiem {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).hour.also {
            return if (it in 0..11) Meridiem.AM else Meridiem.PM
        }
    } else {
        Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = alarmTimeInMillis
            return if (get(Calendar.AM_PM) == Calendar.AM) Meridiem.AM else Meridiem.PM
        }
    }
}