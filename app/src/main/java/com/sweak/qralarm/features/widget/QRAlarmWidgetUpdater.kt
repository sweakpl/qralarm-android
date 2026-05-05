package com.sweak.qralarm.features.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.ALARM_LABEL_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.ALARM_TIME_MILLIS_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.WIDGET_STATE_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.WidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class QRAlarmWidgetUpdater @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    @param:ApplicationContext private val appContext: Context
) {

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
        val glanceIds = GlanceAppWidgetManager(appContext)
            .getGlanceIds(QRAlarmWidget::class.java)
        if (glanceIds.isEmpty()) return

        val widget = QRAlarmWidget()
        val (nextAlarm, isSnoozed) = getNextAlarm()

        glanceIds.forEach { id ->
            updateAppWidgetState(appContext, id) { prefs ->
                when {
                    nextAlarm == null -> {
                        prefs[WIDGET_STATE_KEY] = WidgetState.NO_ALARM.name
                        prefs.remove(ALARM_TIME_MILLIS_KEY)
                        prefs.remove(ALARM_LABEL_KEY)
                    }
                    isSnoozed -> {
                        prefs[WIDGET_STATE_KEY] = WidgetState.SNOOZED.name
                        prefs[ALARM_TIME_MILLIS_KEY] =
                            nextAlarm.snoozeConfig.nextSnoozedAlarmTimeInMillis ?: 0L
                        prefs.remove(ALARM_LABEL_KEY)
                    }
                    else -> {
                        prefs[WIDGET_STATE_KEY] = WidgetState.NORMAL.name
                        prefs[ALARM_TIME_MILLIS_KEY] = nextAlarm.nextAlarmTimeInMillis
                        val label = nextAlarm.alarmLabel
                        if (label != null) prefs[ALARM_LABEL_KEY] = label
                        else prefs.remove(ALARM_LABEL_KEY)
                    }
                }
            }
            widget.update(appContext, id)
        }
    }

    private suspend fun getNextAlarm(): Pair<Alarm?, Boolean> {
        val alarmsList = alarmsRepository.getAllAlarms().first()

        val enabledNextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled }
            .minByOrNull { it.nextAlarmTimeInMillis }

        val snoozedNextAlarm = alarmsList
            .filter {
                it.snoozeConfig.nextSnoozedAlarmTimeInMillis != null &&
                it.snoozeConfig.isAlarmSnoozed
            }
            .minByOrNull { it.snoozeConfig.nextSnoozedAlarmTimeInMillis ?: Long.MAX_VALUE }

        val enabledNextAlarmTime = enabledNextAlarm?.nextAlarmTimeInMillis ?: Long.MAX_VALUE
        val snoozedNextAlarmTime = snoozedNextAlarm?.snoozeConfig?.nextSnoozedAlarmTimeInMillis
            ?: Long.MAX_VALUE

        return if (enabledNextAlarm == null && snoozedNextAlarm == null) {
            Pair(null, false)
        } else if (enabledNextAlarmTime < snoozedNextAlarmTime) {
            Pair(enabledNextAlarm, false)
        } else {
            Pair(snoozedNextAlarm, true)
        }
    }
}
