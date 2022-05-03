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
        applySystemAlarmRemovalUpdateIfRequired()
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
            putInt(DataStoreManager.ALARM_SOUND, AlarmSound.GENTLE_GUITAR.ordinal)
        }
    }

    private fun applySystemAlarmRemovalUpdateIfRequired() {
        val systemAlarmRemovalUpdateRequired = runBlocking {
            dataStoreManager.getBoolean(
                DataStoreManager.SYSTEM_ALARM_REMOVAL_UPDATE_REQUIRED
            ).first()
        }

        if (systemAlarmRemovalUpdateRequired) {
            runBlocking {
                dataStoreManager.apply {
                    /* Update 1.0.1 removes DEFAULT_SYSTEM alarm which had selection integer = 0.
                     * This forces the app to shift the preference selection integer:
                     * If it was 0, it stays 0. If it was anything but 0 it has 1 subtracted from it
                     * This way, the old selection integers are mapped to new selection integers.
                     * If the user had DEFAULT_SYSTEM selected they will have GENTLE_GUITAR now */
                    val previousAlarmPreferenceInt = getInt(DataStoreManager.ALARM_SOUND).first()
                    val newAlarmPreferenceInt = previousAlarmPreferenceInt.let {
                        if (it != 0) it - 1 else 0
                    }

                    putInt(DataStoreManager.ALARM_SOUND, newAlarmPreferenceInt)
                    putBoolean(DataStoreManager.SYSTEM_ALARM_REMOVAL_UPDATE_REQUIRED, false)
                }
            }
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
}