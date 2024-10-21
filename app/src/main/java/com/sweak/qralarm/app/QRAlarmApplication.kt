package com.sweak.qralarm.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.compose.ui.graphics.toArgb
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QRAlarmApplication : Application() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
}