package com.sweak.qralarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.text.format.DateFormat
import androidx.compose.ui.graphics.toArgb
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.TimeFormat
import com.sweak.qralarm.util.currentTimeInMillis
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

        setUpPreferencesIfFirstLaunch()
        createNotificationChannelIfVersionRequires()
    }

    private fun setUpPreferencesIfFirstLaunch() {
        val firstLaunch = runBlocking {
            dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()
        }

        if (firstLaunch) {
            runBlocking {
                setDefaultAlarmLifecyclePreferences()
                setDefaultAlarmTimePreferences()
                dataStoreManager.putBoolean(DataStoreManager.FIRST_LAUNCH, false)
            }
        }
    }

    private suspend fun setDefaultAlarmLifecyclePreferences() {
        dataStoreManager.apply {
            putBoolean(DataStoreManager.ALARM_SET, false)
            putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, false)
        }
    }

    private suspend fun setDefaultAlarmTimePreferences() {
        val timeInMillis = currentTimeInMillis()
        val timeFormat =
            if (DateFormat.is24HourFormat(this)) TimeFormat.MILITARY.name
            else TimeFormat.AMPM.name

        dataStoreManager.apply {
            putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, timeInMillis)
            putString(DataStoreManager.ALARM_TIME_FORMAT, timeFormat)
            putInt(DataStoreManager.SNOOZE_MAX_COUNT, 3)
        }
    }

    private fun createNotificationChannelIfVersionRequires() {
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