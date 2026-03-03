package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.getTimeString
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QRAlarmWidgetEntryPoint {
    fun alarmsRepository(): AlarmsRepository
}
class QRAlarmWidgetUpdater(appContext: Context) {

    private val appContext: Context = appContext.applicationContext

    val entryPoint = EntryPointAccessors.fromApplication(
        appContext.applicationContext,
        QRAlarmWidgetEntryPoint::class.java
    )

    val alarmsRepository = entryPoint.alarmsRepository()

    suspend fun update() {
        val nextAlarm = getNextAlarm(alarmsRepository)

        broadcastToReceiver(nextAlarm)
    }

    private fun broadcastToReceiver(nextAlarm: Alarm?) {

        Log.d("intent", "${nextAlarm!!.nextAlarmTimeInMillis}")
        Log.d("intent", "${nextAlarm.alarmLabel}")

        val time = getTimeString(nextAlarm.nextAlarmTimeInMillis, DateFormat.is24HourFormat(appContext))
        val label = nextAlarm.alarmLabel

        val intent = Intent(appContext, QRAlarmWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra("time", time)
            putExtra("label", label)
        }
        appContext.sendBroadcast(intent)
    }

    private suspend fun getNextAlarm(alarmsRepository: AlarmsRepository): Alarm? {
        val alarmsList = alarmsRepository.getAllAlarms().first()

        val nextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled }
            .minByOrNull { it.nextAlarmTimeInMillis }

        return nextAlarm
    }
}