package com.sweak.qralarm.features.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.alarm.ACTION_WIDGET_MIDNIGHT_UPDATE
import com.sweak.qralarm.alarm.WIDGET_MIDNIGHT_UPDATE_REQUEST_CODE
import android.os.Build
import android.os.UserManager
import android.text.format.DateFormat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.getDayString
import com.sweak.qralarm.core.ui.getTimeString
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.BOTTOM_TEXT_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.TIME_TEXT_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.TOP_LABEL_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.WIDGET_STATE_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.WidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRAlarmWidgetUpdater @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val alarmManager: AlarmManager,
    @param:ApplicationContext private val appContext: Context
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null
    private val debounceDelayMs = 1500L

    fun requestUpdate() {
        // AppWidgetManager throws IllegalStateException before user unlock (Direct Boot).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val userManager = appContext.getSystemService(Context.USER_SERVICE) as UserManager
            if (!userManager.isUserUnlocked) return
        }

        updateJob?.cancel()
        updateJob = scope.launch {
            delay(debounceDelayMs.milliseconds)
            performUpdate()
        }
    }

    private suspend fun performUpdate() {
        val glanceIds = GlanceAppWidgetManager(appContext)
            .getGlanceIds(QRAlarmWidget::class.java)
        if (glanceIds.isEmpty()) return

        val widget = QRAlarmWidget()
        val resolved = resolveWidgetData()

        glanceIds.forEach { id ->
            updateAppWidgetState(appContext, id) { prefs ->
                prefs[WIDGET_STATE_KEY] = resolved.state.name
                prefs[TOP_LABEL_KEY] = resolved.topLabel
                prefs[TIME_TEXT_KEY] = resolved.timeText
                prefs[BOTTOM_TEXT_KEY] = resolved.bottomText
            }
            widget.update(appContext, id)
        }
    }

    private suspend fun resolveWidgetData(): ResolvedWidgetData {
        val (nextAlarm, isSnoozed) = getNextAlarm()
        val is24h = DateFormat.is24HourFormat(appContext)

        return when {
            nextAlarm == null -> ResolvedWidgetData(
                state = WidgetState.NO_ALARM,
                topLabel = appContext.getString(R.string.no_alarms),
                timeText = NO_TIME_PLACEHOLDER,
                bottomText = appContext.getString(R.string.widget_tap_to_add)
            )

            isSnoozed -> {
                val timeMillis = nextAlarm.snoozeConfig.nextSnoozedAlarmTimeInMillis ?: 0L
                ResolvedWidgetData(
                    state = WidgetState.SNOOZED,
                    topLabel = appContext.getString(R.string.snooze),
                    timeText = getTimeString(timeMillis, is24h),
                    bottomText = resolveDayString(timeMillis)
                )
            }

            else -> {
                val timeMillis = nextAlarm.nextAlarmTimeInMillis
                ResolvedWidgetData(
                    state = WidgetState.NORMAL,
                    topLabel = nextAlarm.alarmLabel
                        ?: appContext.getString(R.string.widget_next_alarm),
                    timeText = getTimeString(timeMillis, is24h),
                    bottomText = resolveDayString(timeMillis)
                )
            }
        }
    }

    private fun resolveDayString(timeMillis: Long): String {
        val alarmDate = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        return when (alarmDate) {
            today -> appContext.getString(R.string.widget_today)
            today.plusDays(1) -> appContext.getString(R.string.widget_tomorrow)
            else -> getDayString(timeMillis)
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

    private data class ResolvedWidgetData(
        val state: WidgetState,
        val topLabel: String,
        val timeText: String,
        val bottomText: String
    )

    fun scheduleMidnightWidgetUpdate() {
        val nextMidnightMillis = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(appContext, QRAlarmWidgetReceiver::class.java).apply {
            action = ACTION_WIDGET_MIDNIGHT_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            WIDGET_MIDNIGHT_UPDATE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextMidnightMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextMidnightMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, nextMidnightMillis, pendingIntent
            )
        }
    }

    companion object {
        private const val NO_TIME_PLACEHOLDER = "--:--"
    }
}
