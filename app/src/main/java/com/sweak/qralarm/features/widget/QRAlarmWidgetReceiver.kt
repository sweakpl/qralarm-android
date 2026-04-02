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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QRAlarmWidgetReceiver : GlanceAppWidgetReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QRAlarmWidgetEntryPoint {
        fun widgetUpdater(): QRAlarmWidgetUpdater
    }


    private fun getWidgetUpdater(context: Context): QRAlarmWidgetUpdater {
        return EntryPointAccessors
            .fromApplication<QRAlarmWidgetEntryPoint>(context.applicationContext)
            .widgetUpdater()
    }

    override val glanceAppWidget: GlanceAppWidget = QRAlarmWidget()

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        receiverScope.launch {
            getWidgetUpdater(context).requestUpdate()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            receiverScope.launch {
                val timeOfNextAlarm: String? = intent.getStringExtra("time")
                val labelOfNextAlarm: String? = intent.getStringExtra("label")

                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(QRAlarmWidget::class.java).also {
                        it.ifEmpty { return@launch }
                    }

                glanceIds.forEach {
                    updateAppWidgetState(context, it) { preferences ->
                        preferences[ALARM_TIME_PREFERENCES_KEY] = timeOfNextAlarm ?: "--:--"
                        preferences[ALARM_LABEL_PREFERENCES_KEY] = labelOfNextAlarm ?: "No alarm set"
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }
}