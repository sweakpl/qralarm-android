package com.sweak.qralarm.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.compose.ui.graphics.toArgb
import androidx.work.Configuration
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.features.widget.QRAlarmWidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QRAlarmApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var widgetUpdater: QRAlarmWidgetUpdater

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
        widgetUpdater.startObserving()
    }

    // The androidx-startup WorkManagerInitializer doesn't run in a process started during
    // Direct Boot, so WorkManager (used by Glance) has to initialize on demand instead.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    private fun createNotificationChannels() {
        val alarmNotificationChannel = NotificationChannel(
            ALARM_NOTIFICATION_CHANNEL_ID,
            getString(R.string.alarm_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            setSound(null, null)
            description = getString(R.string.alarm_notification_channel_description)
            lightColor = Jacarta.toArgb()
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val alarmSetIndicationNotificationChannel = NotificationChannel(
            ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID,
            getString(R.string.alarm_set_indication_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            description =
                getString(R.string.alarm_set_indication_notification_channel_description)
        }

        notificationManager.createNotificationChannels(
            listOf(alarmNotificationChannel, alarmSetIndicationNotificationChannel)
        )
    }
}