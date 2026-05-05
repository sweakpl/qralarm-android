package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
    }
}
