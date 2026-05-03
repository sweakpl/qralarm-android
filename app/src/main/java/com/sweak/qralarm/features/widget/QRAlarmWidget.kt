package com.sweak.qralarm.features.widget

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.sweak.qralarm.R
import com.sweak.qralarm.app.activity.MainActivity
import com.sweak.qralarm.core.designsystem.theme.Nobel
import com.sweak.qralarm.core.ui.getDayString
import com.sweak.qralarm.core.ui.getTimeString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class QRAlarmWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val stateString = prefs[WIDGET_STATE_KEY] ?: WidgetState.NO_ALARM.name
            val state = runCatching { WidgetState.valueOf(stateString) }
                .getOrDefault(WidgetState.NO_ALARM)
            val alarmMillis = prefs[ALARM_TIME_MILLIS_KEY]
            val alarmLabel = prefs[ALARM_LABEL_KEY]

            WidgetContent(state, alarmMillis, alarmLabel)
        }
    }

    companion object {
        val WIDGET_STATE_KEY = stringPreferencesKey("widgetStateKey")
        val ALARM_TIME_MILLIS_KEY = longPreferencesKey("alarmTimeMillisKey")
        val ALARM_LABEL_KEY = stringPreferencesKey("alarmLabelKey")
    }

    enum class WidgetState { NORMAL, SNOOZED, NO_ALARM }
}

@Composable
private fun WidgetContent(
    state: QRAlarmWidget.WidgetState,
    alarmMillis: Long?,
    alarmLabel: String?
) {
    val context = LocalContext.current
    val nobelColorProvider = ColorProvider(day = Nobel, night = Nobel)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>())
            .background(ImageProvider(R.drawable.widget_background))
            .padding(12.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                val topLabel = when (state) {
                    QRAlarmWidget.WidgetState.NORMAL ->
                        alarmLabel ?: context.getString(R.string.widget_next_alarm)
                    QRAlarmWidget.WidgetState.SNOOZED ->
                        context.getString(R.string.snooze)
                    QRAlarmWidget.WidgetState.NO_ALARM ->
                        context.getString(R.string.no_alarms)
                }
                Text(
                    text = topLabel,
                    style = WidgetStyles.label,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(end = 4.dp),
                    maxLines = 1
                )
                if (state != QRAlarmWidget.WidgetState.NO_ALARM) {
                    val iconRes = if (state == QRAlarmWidget.WidgetState.NORMAL) {
                        R.drawable.ic_qralarm
                    } else {
                        R.drawable.ic_snooze
                    }
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = ColorFilter.tint(nobelColorProvider)
                    )
                }
            }

            Spacer(GlanceModifier.defaultWeight())

            if (state == QRAlarmWidget.WidgetState.NO_ALARM) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_alarm_off),
                        contentDescription = null,
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = ColorFilter.tint(nobelColorProvider)
                    )
                }
            } else {
                val is24h = DateFormat.is24HourFormat(context)
                val timeString = alarmMillis?.let { getTimeString(it, is24h) } ?: "--:--"
                Text(text = timeString, style = WidgetStyles.time)
            }

            Spacer(GlanceModifier.defaultWeight())

            Text(
                text = buildBottomText(context, state, alarmMillis),
                style = WidgetStyles.body
            )
        }
    }
}

private fun buildBottomText(
    context: Context,
    state: QRAlarmWidget.WidgetState,
    alarmMillis: Long?
): String {
    return when (state) {
        QRAlarmWidget.WidgetState.NO_ALARM ->
            context.getString(R.string.widget_tap_to_add)

        QRAlarmWidget.WidgetState.NORMAL,
        QRAlarmWidget.WidgetState.SNOOZED -> {
            if (alarmMillis == null) return ""
            val alarmDate = Instant.ofEpochMilli(alarmMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val today = LocalDate.now()
            when (alarmDate) {
                today -> context.getString(R.string.widget_today)
                today.plusDays(1) -> context.getString(R.string.widget_tomorrow)
                else -> getDayString(alarmMillis)
            }
        }
    }
}
