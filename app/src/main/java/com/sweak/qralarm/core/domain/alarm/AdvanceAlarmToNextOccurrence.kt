package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class AdvanceAlarmToNextOccurrence @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm
) {
    suspend operator fun invoke(alarmId: Long) {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return

        when (alarm.repeatingMode) {
            is Alarm.RepeatingMode.Once -> disableAlarm(alarmId = alarmId)
            is Alarm.RepeatingMode.Days -> setAlarm(
                alarmId = alarmId,
                isReschedulingMissedAlarm = false
            )
        }
    }
}
