package com.sweak.qralarm.features.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QRAlarmWidgetReceiver : GlanceAppWidgetReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QRAlarmWidgetEntryPoint {
        fun widgetUpdater(): QRAlarmWidgetUpdater
    }

    override val glanceAppWidget: GlanceAppWidget = QRAlarmWidget()

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        receiverScope.launch {
            EntryPointAccessors
                .fromApplication<QRAlarmWidgetEntryPoint>(context.applicationContext)
                .widgetUpdater()
                .requestUpdate()
        }
    }
}
