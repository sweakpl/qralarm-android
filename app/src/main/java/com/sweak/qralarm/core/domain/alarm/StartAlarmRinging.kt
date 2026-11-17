package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class StartAlarmRinging @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val advanceAlarmToNextOccurrence: AdvanceAlarmToNextOccurrence
) {
    suspend operator fun invoke(alarmId: Long, isSnoozeAlarm: Boolean?) {
        alarmsRepository.setAlarmRunning(alarmId = alarmId, running = true)

        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return

        if (isSnoozeAlarm == false) {
            alarmsRepository.setAvailableSnoozes(
                alarmId = alarmId,
                availableSnoozes = alarm.snoozeConfig.snoozeMode.numberOfSnoozes
            )
        }

        alarmsRepository.setAlarmSnoozed(alarmId = alarmId, snoozed = false)

        advanceAlarmToNextOccurrence(alarmId = alarmId)
    }
}
