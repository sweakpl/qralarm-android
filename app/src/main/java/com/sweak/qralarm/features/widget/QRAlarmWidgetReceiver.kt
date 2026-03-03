package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
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

        // TODO: get Intent extras from broadcast (Updater class)

        // this is where the intent from QRAlarmWidgetUpdater gets processed!
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // TO DO: get glanceIds
            receiverScope.launch {

                val time: String? = intent.getStringExtra("time")
                val label: String? = intent.getStringExtra("label")

                Log.d("time", time.toString())
                Log.d("label", label.toString())

                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(QRAlarmWidget::class.java).also {
                        it.ifEmpty { return@launch }
                    }
                // TO DO: loop through each widget (or just the one) and update their state
                glanceIds.forEach {
                    updateAppWidgetState(context, it) { preferences ->
                        // TODO: update with real data below
                        preferences[ALARM_TIME_PREFERENCES_KEY] = time ?: "null"
                        preferences[ALARM_LABEL_PREFERENCES_KEY] = label ?: "null"
                    }
                    // TO DO: call glanceAppWidget.update() at end with (context, glanceId)
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }
}