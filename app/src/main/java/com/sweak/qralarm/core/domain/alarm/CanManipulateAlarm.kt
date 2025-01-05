package com.sweak.qralarm.core.domain.alarm

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CanManipulateAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long): Boolean {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return false

        if (!alarm.isUsingCode || !alarm.isAlarmEnabled || !alarm.isOneHourLockEnabled) return true

        val alarmDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(alarm.nextAlarmTimeInMillis),
            ZoneId.systemDefault()
        )
        val currentDateTime = ZonedDateTime.now()

        val hoursUntilAlarm = currentDateTime.until(alarmDateTime, ChronoUnit.HOURS)

        return hoursUntilAlarm > 0
    }
}