package com.sweak.qralarm.features.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.sweak.qralarm.R
import androidx.glance.background
import androidx.glance.layout.Box
import com.sweak.qralarm.R.drawable.widget_background
import androidx.glance.action.actionStartActivity
import com.sweak.qralarm.app.activity.MainActivity

class QRAlarmWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {

            val preferences = currentState<androidx.datastore.preferences.core.Preferences>()

            val alarmTime =
                preferences[ALARM_TIME_PREFERENCES_KEY] ?: "No alarm set"

            val alarmLabel =
                preferences[ALARM_LABEL_PREFERENCES_KEY] ?: ""
            Box(
                GlanceModifier.clickable(
                    actionStartActivity<MainActivity>()
                ), contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .background(
                            androidx.glance.ImageProvider(R.drawable.widget_background)
                        )
                ) {
                    Text(
                        text = alarmTime,
                        style = WidgetStyles.time
                    )
                    Text(
                        text = alarmLabel,
                        style = WidgetStyles.title
                    )
                }
            }
        }
    }

    companion object {
        val ALARM_TIME_PREFERENCES_KEY = stringPreferencesKey("alarmTimeKey")
        val ALARM_LABEL_PREFERENCES_KEY = stringPreferencesKey("alarmLabelKey")
    }
}