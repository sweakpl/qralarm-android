package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import javax.inject.Inject

class DisableAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId)

        qrAlarmManager.cancelAlarm(alarmId = alarmId)

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