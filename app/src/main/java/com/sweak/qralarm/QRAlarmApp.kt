package com.sweak.qralarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.text.format.DateFormat
import androidx.compose.ui.graphics.toArgb
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.*
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
            putBoolean(DataStoreManager.ALARM_SNOOZED, false)
            putString(DataStoreManager.DISMISS_ALARM_CODE, DEFAULT_DISMISS_ALARM_CODE)
        }
    }

    private suspend fun setDefaultAlarmTimePreferences() {
        val timeInMillis = currentTimeInMillis()
        val timeFormat =
            if (DateFormat.is24HourFormat(this)) TimeFormat.MILITARY.ordinal
            else TimeFormat.AMPM.ordinal

        dataStoreManager.apply {
            putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, timeInMillis)
            putInt(DataStoreManager.ALARM_TIME_FORMAT, timeFormat)
            putInt(DataStoreManager.SNOOZE_MAX_COUNT, SNOOZE_MAX_COUNT_3)
            putInt(DataStoreManager.SNOOZE_DURATION_MINUTES, SNOOZE_DURATION_10_MINUTES)
            putInt(DataStoreManager.ALARM_SOUND, AlarmSound.DEFAULT_SYSTEM.ordinal)
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