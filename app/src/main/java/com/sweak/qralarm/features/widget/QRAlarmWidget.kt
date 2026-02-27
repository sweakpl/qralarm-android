package com.sweak.qralarm.features.widget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

        provideContent {
            val alarms by alarmsRepository.getAllAlarms().collectAsState(
                initial = emptyList()
            )

            val nextAlarm = getNextAlarm(alarms)

            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (nextAlarm != null) {
                    val hourOfDay = nextAlarm.alarmHourOfDay
                    val minute = nextAlarm.alarmMinute ?: 0
                    val is24HourFormat = DateFormat.is24HourFormat(context)
                    Text(getTimeString(hourOfDay, minute, is24HourFormat))
                } else {
                    Text("No alarms set")
                }
            }


        }
    }

    fun getNextAlarm(alarmsList: List<Alarm>): Alarm? {
        val nextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled }
            .minByOrNull { it.nextAlarmTimeInMillis }
        return nextAlarm
    }
}