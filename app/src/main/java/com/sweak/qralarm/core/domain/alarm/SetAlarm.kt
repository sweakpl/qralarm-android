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

        val alarmTimeInMillis = when (alarm.repeatingMode) {
            is Alarm.RepeatingMode.Once -> alarm.repeatingMode.alarmDayInMillis
            is Alarm.RepeatingMode.Days -> {
                val todayDateTime = ZonedDateTime.now()
                var alarmDateTime = ZonedDateTime.now()
                    .withHour(alarm.alarmHourOfDay)
                    .withMinute(alarm.alarmMinute)
                    .withSecond(0)
                    .withNano(0)

                while (alarmDateTime < todayDateTime ||
                    alarmDateTime.dayOfWeek !in alarm.repeatingMode.repeatingDaysOfWeek
                ) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
        }

        qrAlarmManager.setAlarm(
            alarmId = alarmId,
            alarmTimeInMillis = alarmTimeInMillis
        )

        if (!alarm.isAlarmEnabled) {
            alarmsRepository.setAlarmEnabled(
                alarmId = alarmId,
                enabled = true
            )
        }

        return Result.Success(alarmTimInMillis = alarmTimeInMillis)
    }

    sealed class Result {
        data class Success(val alarmTimInMillis: Long) : Result()
        data object Failure : Result()
    }
}