package com.sweak.qralarm.core.domain.alarm

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.sweak.qralarm.features.widget.QRAlarmWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CopyAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmsRepository.getAlarm(alarmId = alarmId)?.let {
            alarmsRepository.addOrEditAlarm(
                alarm = it.copy(
                    alarmId = 0,
                    isAlarmEnabled = false,
                    isAlarmRunning = false,
                    skipAlarmUntilTimeInMillis = null
                )
            )
        }
        QRAlarmWidget().updateAll(context)
    }
}