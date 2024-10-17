package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class SetAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long): Result {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return Result.Failure

        val alarmTimeInMillis = when (alarm.repeatingMode) {
            is Alarm.RepeatingMode.Once -> {
                var alarmDateTime = Instant
                    .ofEpochMilli(alarm.repeatingMode.alarmDayInMillis)
                    .atZone(ZoneId.systemDefault())
                val currentDateTime = ZonedDateTime.now()

                if (alarmDateTime <= currentDateTime) {
                    alarmDateTime = alarmDateTime.plusDays(1)

                    alarmsRepository.addOrEditAlarm(
                        alarm = alarm.copy(
                            repeatingMode = alarm.repeatingMode.copy(
                                alarmDayInMillis = alarmDateTime.toInstant().toEpochMilli()
                            )
                        )
                    )
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
            is Alarm.RepeatingMode.Days -> {
                val currentDateTime = ZonedDateTime.now()
                var alarmDateTime = ZonedDateTime.now()
                    .withHour(alarm.alarmHourOfDay)
                    .withMinute(alarm.alarmMinute)
                    .withSecond(0)
                    .withNano(0)

                while (alarmDateTime <= currentDateTime ||
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