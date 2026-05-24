package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.sweak.qralarm.alarm.ACTION_WIDGET_MIDNIGHT_UPDATE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QRAlarmWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var widgetUpdater: QRAlarmWidgetUpdater

    override val glanceAppWidget: GlanceAppWidget = QRAlarmWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        widgetUpdater.requestUpdate()
        widgetUpdater.scheduleMidnightWidgetUpdate()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == ACTION_WIDGET_MIDNIGHT_UPDATE
        ) {
            widgetUpdater.requestUpdate()
            widgetUpdater.scheduleMidnightWidgetUpdate()
        }
    }
}
