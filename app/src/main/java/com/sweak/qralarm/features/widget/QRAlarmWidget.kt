package com.sweak.qralarm.features.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.text.Text
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

class QRAlarmWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QRAlarmWidgetEntryPoint::class.java
        )

        val alarmsRepository = entryPoint.alarmsRepository()

        val allAlarmsFlow = alarmsRepository.getAllAlarms()

        val alarmAsList = allAlarmsFlow.first()

        val nextAlarm = getNextAlarm(alarmAsList)

        val nextAlarmTimeString = getNextAlarmTimeAsString(nextAlarm)

        provideContent {
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hello World")
                // Text(anAlarmList[0].alarmLabel.toString())

                // displays the next alarm time!!
                Text(nextAlarmTimeString)
            }


        }
    }

    fun getNextAlarm(alarmsList: List<Alarm>): Alarm? {
        val nextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled }
            .minByOrNull { it.nextAlarmTimeInMillis }
        return nextAlarm
    }

    // TODO: find out how to see if an alarm is 24HourFormat or not?
    fun getNextAlarmTimeAsString(alarm: Alarm?): String {
        return getTimeString(alarm?.nextAlarmTimeInMillis ?: 0, is24HourFormat = false)
    }
}