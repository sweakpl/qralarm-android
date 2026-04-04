package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
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
import kotlin.math.min


class QRAlarmWidgetUpdater @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    @ApplicationContext private val appContext: Context)
{

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var updateJob: Job? = null

    private val debounceDelayMs = 1500L

    fun requestUpdate() {
        updateJob?.cancel()

        updateJob = scope.launch {
            delay(debounceDelayMs)
            performUpdate()
        }
    }

    private suspend fun performUpdate() {
        val nextAlarm: Pair<Alarm?, Boolean> = getNextAlarm(alarmsRepository)
        broadcastToReceiver(nextAlarm)
    }

    private fun broadcastToReceiver(alarmPair: Pair<Alarm?, Boolean>) {
        var time = "--:--"
        var label: String

        val nextAlarm = alarmPair.first
        val isNextAlarmSnoozed = alarmPair.second

        if (nextAlarm != null && isNextAlarmSnoozed) {
            nextAlarm.snoozeConfig.nextSnoozedAlarmTimeInMillis?.let {
                time = getTimeString(
                    it,
                    DateFormat.is24HourFormat(appContext)
                )
            }
            label = "Snoozed: ${nextAlarm.alarmLabel ?: "No label"}"
        } else if (nextAlarm != null) {
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
    ): Pair<Alarm?, Boolean> {
        val alarmsList = alarmsRepository.getAllAlarms().first()

        val enabledNextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled}
            .minByOrNull { it.nextAlarmTimeInMillis }

        val snoozedNextAlarm = alarmsList
            .filter { it.snoozeConfig.nextSnoozedAlarmTimeInMillis != null }
            .minByOrNull { it.snoozeConfig.nextSnoozedAlarmTimeInMillis!! }

        val enabledNextAlarmTime = enabledNextAlarm?.nextAlarmTimeInMillis ?: Long.MAX_VALUE

        val snoozedNextAlarmTime = snoozedNextAlarm?.snoozeConfig?.nextSnoozedAlarmTimeInMillis
            ?: Long.MAX_VALUE

        if (enabledNextAlarm == null && snoozedNextAlarm == null) {
            return Pair(null, false)
        } else if (enabledNextAlarmTime < snoozedNextAlarmTime) {
            return Pair(enabledNextAlarm, false)
        } else {
            return Pair(snoozedNextAlarm, true)
        }
    }
}