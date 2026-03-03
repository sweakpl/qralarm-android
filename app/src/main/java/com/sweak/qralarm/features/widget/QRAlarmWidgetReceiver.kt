package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.ALARM_LABEL_PREFERENCES_KEY
import com.sweak.qralarm.features.widget.QRAlarmWidget.Companion.ALARM_TIME_PREFERENCES_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QRAlarmWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QRAlarmWidget()

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // this is where the intent from QRAlarmWidgetUpdater gets processed!
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            receiverScope.launch {

                val timeOfNextAlarm: String? = intent.getStringExtra("time")
                val labelOfNextAlarm: String? = intent.getStringExtra("label")

                // Log.d("timeOfNextAlarm", timeOfNextAlarm.toString())
                // Log.d("labelOfNextAlarm", labelOfNextAlarm.toString())

                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(QRAlarmWidget::class.java).also {
                        it.ifEmpty { return@launch }
                    }

                glanceIds.forEach {
                    updateAppWidgetState(context, it) { preferences ->
                        // TODO: update with real data below
                        preferences[ALARM_TIME_PREFERENCES_KEY] = timeOfNextAlarm ?: "null"
                        preferences[ALARM_LABEL_PREFERENCES_KEY] = labelOfNextAlarm ?: "null"
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }
}