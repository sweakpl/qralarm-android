package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class CopyAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmsRepository.getAlarm(alarmId = alarmId)?.let {
            alarmsRepository.addOrEditAlarm(
                alarm = it.copy(
                    alarmId = 0,
                    isAlarmEnabled = false,
                    isAlarmRunning = false,
                    skipAlarmUntilTimeInMillis = null
                )
            )
        }
    }
}