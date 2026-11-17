package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class HandleEarlyStoppedAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val advanceAlarmToNextOccurrence: AdvanceAlarmToNextOccurrence
) {
    suspend operator fun invoke(alarmId: Long) {
        // The delivery is already consumed and the regular rescheduling at ring start will not
        // run, so the next occurrence has to be rescheduled here:
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return

        if (alarm.snoozeConfig.isAlarmSnoozed) {
            alarmsRepository.setAlarmSnoozed(alarmId = alarmId, snoozed = false)
        }

        advanceAlarmToNextOccurrence(alarmId = alarmId)
    }
}
