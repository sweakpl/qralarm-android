package com.sweak.qralarm.core.domain.alarm

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.features.widget.QRAlarmWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DisableAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository,
    @ApplicationContext private val context: Context
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
        QRAlarmWidget().updateAll(context)
    }
}