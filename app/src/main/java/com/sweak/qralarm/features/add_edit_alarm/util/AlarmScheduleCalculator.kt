package com.sweak.qralarm.features.add_edit_alarm.util

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.ui.getEarliestOnlyOnceAlarmDate
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

class AlarmScheduleCalculator @Inject constructor(
    private val clock: Clock
) {
    fun computeNextAlarmTimeInMillis(
        hourOfDay: Int,
        minute: Int,
        repeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
        onlyOnceAlarmDateInMillis: Long
    ): Long {
        val currentDateTime = ZonedDateTime.now(clock)

        return when (val repeatingMode = repeatingScheduleWrapper.toRepeatingMode()) {
            is Alarm.RepeatingMode.Once -> {
                var alarmDateTime = Instant.ofEpochMilli(onlyOnceAlarmDateInMillis)
                    .atZone(clock.zone)
                    .toLocalDate()
                    .atTime(hourOfDay, minute)
                    .atZone(clock.zone)

                if (alarmDateTime <= currentDateTime) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }

            is Alarm.RepeatingMode.Days -> {
                var alarmDateTime = currentDateTime
                    .withHour(hourOfDay)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0)

                while (alarmDateTime <= currentDateTime ||
                    alarmDateTime.dayOfWeek !in repeatingMode.repeatingDaysOfWeek
                ) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
        }
    }

    fun resolveOnlyOnceAlarmDateInMillis(
        currentDateInMillis: Long,
        hourOfDay: Int,
        minute: Int,
        isDateSticky: Boolean
    ): Long {
        val earliestDate = getEarliestOnlyOnceAlarmDate(
            hourOfDay = hourOfDay,
            minute = minute,
            now = ZonedDateTime.now(clock)
        )
        val date = if (isDateSticky) {
            earliestDate
        } else {
            Instant.ofEpochMilli(currentDateInMillis)
                .atZone(clock.zone)
                .toLocalDate()
                .let { if (it.isBefore(earliestDate)) earliestDate else it }
        }

        return date
            .atTime(hourOfDay, minute)
            .atZone(clock.zone)
            .toInstant()
            .toEpochMilli()
    }
}

fun AlarmRepeatingScheduleWrapper.toRepeatingMode(): Alarm.RepeatingMode =
    when (alarmRepeatingMode) {
        ONLY_ONCE -> Alarm.RepeatingMode.Once
        MON_FRI -> Alarm.RepeatingMode.Days(
            repeatingDaysOfWeek = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
        )

        SAT_SUN -> Alarm.RepeatingMode.Days(
            repeatingDaysOfWeek = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        EVERYDAY -> Alarm.RepeatingMode.Days(repeatingDaysOfWeek = DayOfWeek.entries)
        CUSTOM -> Alarm.RepeatingMode.Days(repeatingDaysOfWeek = alarmDaysOfWeek)
    }
