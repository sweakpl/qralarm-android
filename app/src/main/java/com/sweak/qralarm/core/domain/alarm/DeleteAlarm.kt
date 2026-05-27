package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import javax.inject.Inject

class DeleteAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val widgetUpdater: QRAlarmWidgetUpdater
) {
    suspend operator fun invoke(alarmId: Long) {
        qrAlarmManager.cancelAlarm(alarmId = alarmId)
        alarmsRepository.deleteAlarm(alarmId = alarmId)
        codesRepository.cleanupUnreferencedCodes()
        widgetUpdater.requestUpdate()
    }
}
