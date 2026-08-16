package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import javax.inject.Inject

class DisableAlarm @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val alarmsRepository: AlarmsRepository,
    private val widgetUpdater: QRAlarmWidgetUpdater
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

        widgetUpdater.requestUpdate()

    }
}
