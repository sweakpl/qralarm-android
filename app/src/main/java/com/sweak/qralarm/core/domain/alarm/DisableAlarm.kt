package com.sweak.qralarm.core.domain.alarm

import android.content.Context
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DisableAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository,
    @ApplicationContext private val appContext: Context
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

        QRAlarmWidgetUpdater(appContext).requestUpdate()

    }
}