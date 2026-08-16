package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class DisableAlarm @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId)

        alarmScheduler.cancelAlarm(alarmId = alarmId)

        if (alarm?.isAlarmEnabled == true) {
            alarmsRepository.setAlarmEnabled(
                alarmId = alarmId,
                enabled = false
            )
        }

        if (alarm?.snoozeConfig?.isAlarmSnoozed == true) {
            alarmsRepository.setAlarmSnoozed(
                alarmId = alarmId,
                snoozed = false
            )
        }
    }
}
