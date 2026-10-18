package com.sweak.qralarm.core.domain.alarm

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CanManipulateAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(alarmId: Long): Boolean {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return false

        if (!alarm.isUsingCode || !alarm.isAlarmEnabled || alarm.cancelLockDurationInMinutes == 0) {
            return true
        }

        val currentDateTime = ZonedDateTime.now(clock)
        val alarmDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(alarm.nextAlarmTimeInMillis),
            currentDateTime.zone
        )

        val minutesUntilAlarm = currentDateTime.until(alarmDateTime, ChronoUnit.MINUTES)

        return minutesUntilAlarm >= alarm.cancelLockDurationInMinutes
    }
}