package com.sweak.qralarm.core.domain.alarm

//import android.content.Context
//import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
//import dagger.hilt.android.qualifiers.ApplicationContext
import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import javax.inject.Inject

class CopyAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val widgetUpdater: QRAlarmWidgetUpdater,
    
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
        widgetUpdater.requestUpdate()
    }
}