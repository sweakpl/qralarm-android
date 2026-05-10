package com.sweak.qralarm.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.sweak.qralarm.R
import com.sweak.qralarm.app.activity.MainActivity
import com.sweak.qralarm.core.designsystem.theme.Nobel

class QRAlarmWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val stateString = prefs[WIDGET_STATE_KEY] ?: WidgetState.NO_ALARM.name
            val state = runCatching { WidgetState.valueOf(stateString) }
                .getOrDefault(WidgetState.NO_ALARM)
            val topLabel = prefs[TOP_LABEL_KEY].orEmpty()
            val timeText = prefs[TIME_TEXT_KEY] ?: "--:--"
            val bottomText = prefs[BOTTOM_TEXT_KEY].orEmpty()

            WidgetContent(state, topLabel, timeText, bottomText)
        }
    }

    companion object {
        val WIDGET_STATE_KEY = stringPreferencesKey("widgetStateKey")
        val TOP_LABEL_KEY = stringPreferencesKey("topLabelKey")
        val TIME_TEXT_KEY = stringPreferencesKey("timeTextKey")
        val BOTTOM_TEXT_KEY = stringPreferencesKey("bottomTextKey")
    }

    enum class WidgetState { NORMAL, SNOOZED, NO_ALARM }
}

private val nobelColorProvider = ColorProvider(day = Nobel, night = Nobel)

@Composable
private fun WidgetContent(
    state: QRAlarmWidget.WidgetState,
    topLabel: String,
    timeText: String,
    bottomText: String
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>())
            .background(ImageProvider(R.drawable.widget_background))
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
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

            val noAlarmSet = state == QRAlarmWidget.WidgetState.NO_ALARM

            Box(
                modifier = GlanceModifier.fillMaxWidth(),
                contentAlignment = if (noAlarmSet) Alignment.Center else Alignment.CenterStart
            ) {
                if (noAlarmSet) {
                    Box(modifier = GlanceModifier.padding(vertical = 4.dp)) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_alarm_off),
                            contentDescription = null,
                            modifier = GlanceModifier.size(24.dp),
                            colorFilter = ColorFilter.tint(nobelColorProvider)
                        )
                    }
                } else {
                    Text(
                        text = timeText,
                        style = WidgetStyles.time,
                    )
                }
            }

            Text(
                text = bottomText,
                style = WidgetStyles.body
            )
        }
    }
}
