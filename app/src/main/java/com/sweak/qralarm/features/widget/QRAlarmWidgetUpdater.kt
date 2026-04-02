package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.getTimeString
//import dagger.hilt.EntryPoint
//import dagger.hilt.InstallIn
//import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch



class QRAlarmWidgetUpdater @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    @ApplicationContext private val appContext: Context)
{

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var updateJob: Job? = null

    private val debounceDelayMs = 1500L



    //work on injection, potentially redo the updater functions!

    fun requestUpdate() {
        updateJob?.cancel()

        updateJob = scope.launch {
            delay(debounceDelayMs)
            performUpdate()
        }
    }

    private suspend fun performUpdate() {
        val nextAlarm = getNextAlarm(alarmsRepository)
        broadcastToReceiver(nextAlarm)
    }

    private fun broadcastToReceiver(nextAlarm: Alarm?) {
        val time: String
        val label: String

        if (nextAlarm != null) {
            time = getTimeString(
                nextAlarm.nextAlarmTimeInMillis,
                DateFormat.is24HourFormat(appContext)
            )
            label = nextAlarm.alarmLabel ?: "No label"
        } else {
            time = "--:--"
            label = "No alarm set"
        }

        val intent = Intent(
            appContext,
            QRAlarmWidgetReceiver::class.java
        ).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra("time", time)
            putExtra("label", label)
        }

        appContext.sendBroadcast(intent)
    }

    private suspend fun getNextAlarm(
        alarmsRepository: AlarmsRepository
    ): Alarm? {
        val alarmsList = alarmsRepository.getAllAlarms().first()

        return alarmsList
            .filter { it.isAlarmEnabled }
            //check to filter snoozed alarms as well!
            .minByOrNull { it.nextAlarmTimeInMillis }
    }

}