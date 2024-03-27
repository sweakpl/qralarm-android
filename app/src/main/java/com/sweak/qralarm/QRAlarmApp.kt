package com.sweak.qralarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.text.format.DateFormat
import androidx.compose.ui.graphics.toArgb
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.*
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import javax.inject.Inject

@HiltAndroidApp
class QRAlarmApp : Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    override fun onCreate() {
        super.onCreate()

        setUpPreferences()
        createNotificationChannelsIfVersionRequires()
        applyCorrectionsIfAlarmServiceWasNotProperlyFinished()
    }

    private fun setUpPreferences() = runBlocking {
        // Always update the timezone
        dataStoreManager.putString(DataStoreManager.ALARM_TIME_ZONE_ID, ZoneId.systemDefault().id)

        val firstLaunch = dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()

        if (firstLaunch) {
            setDefaultAlarmLifecyclePreferences()
            setDefaultAlarmTimePreferences()
            setDefaultAlarmSoundPreferences()
            setDefaultUsabilityPreferences()
        }
    }

    private suspend fun setDefaultUsabilityPreferences() {
        dataStoreManager.putBoolean(DataStoreManager.ACCEPT_ANY_CODE_TYPE, false)
    }

    private suspend fun setDefaultAlarmLifecyclePreferences() {
        dataStoreManager.apply {
            putBoolean(DataStoreManager.ALARM_SET, false)
            putBoolean(DataStoreManager.ALARM_SNOOZED, false)
            putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, false)
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
            putInt(DataStoreManager.SNOOZE_MAX_COUNT, SnoozeMaxCount.SNOOZE_MAX_COUNT_3.count)
            putInt(
                DataStoreManager.SNOOZE_DURATION_MINUTES,
                SnoozeDuration.SNOOZE_DURATION_10_MINUTES.lengthMinutes
            )
        }
    }

    private suspend fun setDefaultAlarmSoundPreferences() {
        dataStoreManager.apply {
            putInt(DataStoreManager.ALARM_SOUND, AlarmSound.GENTLE_GUITAR.ordinal)
            putInt(
                DataStoreManager.GENTLE_WAKEUP_DURATION_SECONDS,
                GentleWakeupDuration.GENTLE_WAKEUP_DURATION_0_SECONDS.lengthSeconds
            )
        }
    }

    private fun createNotificationChannelsIfVersionRequires() {
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

            notificationManager.createNotificationChannel(alarmNotificationChannel)
            notificationManager.createNotificationChannel(alarmSetIndicationNotificationChannel)
        }
    }

    private fun applyCorrectionsIfAlarmServiceWasNotProperlyFinished() {
        runBlocking {
            dataStoreManager.apply {
                if (!getBoolean(DataStoreManager.ALARM_SERVICE_PROPERLY_CLOSED).first()) {
                    qrAlarmManager.removeAlarmPendingIntent()
                    putBoolean(DataStoreManager.ALARM_SET, false)
                    putBoolean(DataStoreManager.ALARM_SNOOZED, false)
                    putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, false)
                    putBoolean(DataStoreManager.ALARM_SERVICE_PROPERLY_CLOSED, true)
                }
            }
        }
    }
}