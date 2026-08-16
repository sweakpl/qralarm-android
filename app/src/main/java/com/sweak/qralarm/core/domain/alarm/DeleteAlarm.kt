package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import javax.inject.Inject

class DeleteAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val alarmScheduler: AlarmScheduler,
    private val widgetUpdater: QRAlarmWidgetUpdater,
    private val alarmRingtoneStorage: AlarmRingtoneStorage
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmScheduler.cancelAlarm(alarmId = alarmId)
        alarmsRepository.deleteAlarm(alarmId = alarmId)
        alarmRingtoneStorage.deleteForAlarm(alarmId = alarmId)
        codesRepository.cleanupUnreferencedCodes()
        widgetUpdater.requestUpdate()
    }
}
