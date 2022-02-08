package com.sweak.qralarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.compose.ui.graphics.toArgb
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.CurrentTime
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class QRAlarmApp : Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        setAlarmTimeToCurrentTimeIfFirstLaunch()
        createNotificationChannelVersionRequires()
    }

    private fun setAlarmTimeToCurrentTimeIfFirstLaunch() {
        val firstLaunch = runBlocking {
            dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()
        }

        if (firstLaunch) {
            val currentTime = CurrentTime(this)

            runBlocking {
                dataStoreManager.apply {
                    putString(DataStoreManager.ALARM_TIME_FORMAT, currentTime.timeFormat.name)
                    putInt(DataStoreManager.ALARM_HOUR, currentTime.hour)
                    putInt(DataStoreManager.ALARM_MINUTE, currentTime.minute)
                    putString(DataStoreManager.ALARM_MERIDIEM, currentTime.meridiem.name)
                    putBoolean(DataStoreManager.ALARM_SET, false)
                    putBoolean(DataStoreManager.FIRST_LAUNCH, false)
                }
            }
        }
    }

    private fun createNotificationChannelVersionRequires() {
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

            notificationManager.createNotificationChannel(alarmNotificationChannel)
        }
    }

    companion object {
        const val ALARM_NOTIFICATION_CHANNEL_ID = "QRAlarmNotificationChannelId"
    }
}