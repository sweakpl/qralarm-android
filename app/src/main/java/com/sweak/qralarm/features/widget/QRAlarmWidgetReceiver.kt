package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class QRAlarmWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QRAlarmWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // TODO: get glanceIds

            // TODO: loop through each widget (or just the one) and update their state


            // TODO: use companion object of widget main class; update the companion object


            // TODO: call glanceAppWidget.update() at end with (context, glanceId)
        }
    }
}