package com.sweak.qralarm.core.domain.alarm

import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

class SnoozeAlarm @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val alarmNotifier: AlarmNotifier,
    private val alarmsRepository: AlarmsRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        alarmId: Long,
        isReschedulingCurrentOrMissedSnooze: Boolean
    ): Result {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return Result.NotAvailable

        if (!isReschedulingCurrentOrMissedSnooze && !alarm.isSnoozeAvailable()) {
            return Result.NotAvailable
        }

        val snoozeAlarmTimeInMillis =
            if (isReschedulingCurrentOrMissedSnooze) alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis
            else ZonedDateTime.now(clock)
                .withSecond(0)
                .withNano(0)
                .plusMinutes(alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes.toLong())
                .toInstant()
                .toEpochMilli()

        alarmsRepository.addOrEditAlarm(
            alarm = alarm.copy(
                snoozeConfig = alarm.snoozeConfig.copy(
                    numberOfSnoozesLeft =
                        if (isReschedulingCurrentOrMissedSnooze) alarm.snoozeConfig.numberOfSnoozesLeft
                        else alarm.snoozeConfig.numberOfSnoozesLeft - 1,
                    isAlarmSnoozed = true,
                    nextSnoozedAlarmTimeInMillis = snoozeAlarmTimeInMillis
                )
            )
        )

        if (snoozeAlarmTimeInMillis != null) {
            alarmScheduler.setAlarm(
                alarmId = alarm.alarmId,
                alarmTimeInMillis = snoozeAlarmTimeInMillis,
                isSnoozeAlarm = true
            )

            val (alarmHourOfDay, alarmMinute) =
                getHourAndMinuteOfAlarmTimeInMillis(snoozeAlarmTimeInMillis, clock.zone)

            alarmNotifier.showUpcomingAlarmNotification(
                alarmId = alarm.alarmId,
                alarmHourOfDay = alarmHourOfDay,
                alarmMinute = alarmMinute,
                isSnoozeAlarm = true
            )
        }

        return Result.Snoozed
    }

    sealed class Result {
        data object Snoozed : Result()
        data object NotAvailable : Result()
    }
}
