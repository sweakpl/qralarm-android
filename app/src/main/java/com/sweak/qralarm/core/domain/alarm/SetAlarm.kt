package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import java.time.ZonedDateTime
import javax.inject.Inject

class SetAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long): Result {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return Result.Failure
        val currentDateTime = ZonedDateTime.now()
        var alarmDateTime = ZonedDateTime.now()
            .withHour(alarm.alarmHourOfDay)
            .withMinute(alarm.alarmMinute)
            .withSecond(0)
            .withNano(0)

        val alarmTimeInMillis = when (alarm.repeatingMode) {
            is Alarm.RepeatingMode.Once -> {
                if (alarmDateTime <= currentDateTime) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
            is Alarm.RepeatingMode.Days -> {
                while (alarmDateTime <= currentDateTime ||
                    alarmDateTime.dayOfWeek !in alarm.repeatingMode.repeatingDaysOfWeek ||
                    alarm.skipAlarmUntilTimeInMillis?.run {
                        return@run alarmDateTime.toInstant().toEpochMilli() <= this
                    } == true
                ) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
        }

        alarmsRepository.addOrEditAlarm(
            alarm = alarm.copy(nextAlarmTimeInMillis = alarmTimeInMillis)
        )

        qrAlarmManager.setAlarm(
            alarmId = alarmId,
            alarmTimeInMillis = alarmTimeInMillis,
            isSnoozeAlarm = false
        )

        if (!alarm.isAlarmEnabled) {
            alarmsRepository.setAlarmEnabled(
                alarmId = alarmId,
                enabled = true
            )
        }

        val upcomingAlarmNotificationTimeInMillis =
            alarmDateTime.minusHours(2).toInstant().toEpochMilli()

        qrAlarmManager.scheduleUpcomingAlarmNotification(
            alarmId = alarmId,
            upcomingAlarmNotificationTimeInMillis = upcomingAlarmNotificationTimeInMillis
        )

        return Result.Success(alarmTimInMillis = alarmTimeInMillis)
    }

    sealed class Result {
        data class Success(val alarmTimInMillis: Long) : Result()
        data object Failure : Result()
    }
}