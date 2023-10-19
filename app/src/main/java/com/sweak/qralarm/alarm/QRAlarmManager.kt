package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.ALARM_INFO_PENDING_INTENT_REQUEST_CODE
import com.sweak.qralarm.util.ALARM_PENDING_INTENT_REQUEST_CODE
import com.sweak.qralarm.util.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.util.ALARM_SET_INDICATION_NOTIFICATION_ID
import com.sweak.qralarm.util.ALARM_SET_INDICATION_NOTIFICATION_REQUEST_CODE
import com.sweak.qralarm.util.CANCEL_ALARM_ACTION_REQUEST_CODE
import com.sweak.qralarm.util.KEY_ALARM_TYPE
import com.sweak.qralarm.util.TESTING_PERMISSION_ALARM_INTENT_REQUEST_CODE
import com.sweak.qralarm.util.TimeFormat
import com.sweak.qralarm.util.currentTimeInMillis
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class QRAlarmManager @Inject constructor(
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManager,
    private val packageManager: PackageManager,
    private val app: Application
) {
    fun setAlarm(alarmTimeInMillis: Long, alarmType: Int) {
        val intentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE
                else 0

        val alarmIntent = Intent(app.applicationContext, QRAlarmService::class.java).apply {
            putExtra(KEY_ALARM_TYPE, alarmType)
        }

        val alarmPendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    app.applicationContext,
                    ALARM_PENDING_INTENT_REQUEST_CODE,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    app.applicationContext,
                    ALARM_PENDING_INTENT_REQUEST_CODE,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                )
            }

        val alarmInfoPendingIntent = PendingIntent.getActivity(
            app.applicationContext,
            ALARM_INFO_PENDING_INTENT_REQUEST_CODE,
            Intent(app.applicationContext, MainActivity::class.java),
            intentFlags
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(alarmTimeInMillis, alarmInfoPendingIntent),
            alarmPendingIntent
        )

        postAlarmSetIndicationNotification(alarmTimeInMillis)

        packageManager.setComponentEnabledSetting(
            ComponentName(app, BootAndUpdateReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun postAlarmSetIndicationNotification(alarmTimeInMillis: Long) {
        val alarmSetIndicationPendingIntent = PendingIntent.getActivity(
            app.applicationContext,
            ALARM_SET_INDICATION_NOTIFICATION_REQUEST_CODE,
            Intent(app.applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val timeFormat =
            if (DateFormat.is24HourFormat(app.applicationContext)) TimeFormat.MILITARY
            else TimeFormat.AMPM

        val alarmTimeLocalizedString = SimpleDateFormat(
            if (timeFormat == TimeFormat.MILITARY) "HH:mm" else "hh:mm a",
            Locale.getDefault()
        ).format(alarmTimeInMillis)

        val cancelAlarmActionPendingIntent = PendingIntent.getBroadcast(
            app.applicationContext,
            CANCEL_ALARM_ACTION_REQUEST_CODE,
            Intent(app.applicationContext, CancelAlarmReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )

        val alarmSetIndicationNotification = NotificationCompat.Builder(
            app.applicationContext,
            ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(true)
            setColorized(true)
            setContentTitle(app.getString(R.string.alarm_set_indication_notification_title))
            setContentText(
                app.getString(
                    R.string.alarm_set_indication_notification_text,
                    alarmTimeLocalizedString
                )
            )
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentIntent(alarmSetIndicationPendingIntent)
            addAction(
                NotificationCompat.Action
                    .Builder(
                        R.drawable.ic_notification_icon,
                        app.getString(R.string.cancel_alarm),
                        cancelAlarmActionPendingIntent
                    )
                    .setAuthenticationRequired(true)
                    .build()
            )
        }.build()

        notificationManager.notify(
            ALARM_SET_INDICATION_NOTIFICATION_ID,
            alarmSetIndicationNotification
        )
    }

    fun cancelAlarm() {
        app.stopService(Intent(app.applicationContext, QRAlarmService::class.java))
        notificationManager.cancel(ALARM_SET_INDICATION_NOTIFICATION_ID)
        removeAlarmPendingIntent()

        packageManager.setComponentEnabledSetting(
            ComponentName(app, BootAndUpdateReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun removeAlarmPendingIntent() {
        val alarmPendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    app.applicationContext,
                    ALARM_PENDING_INTENT_REQUEST_CODE,
                    Intent(app.applicationContext, QRAlarmService::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    app.applicationContext,
                    ALARM_PENDING_INTENT_REQUEST_CODE,
                    Intent(app.applicationContext, QRAlarmService::class.java),
                    PendingIntent.FLAG_NO_CREATE or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                )
            }

        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent)
            alarmPendingIntent.cancel()
        }
    }

    fun isAlarmSet(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                app.applicationContext,
                ALARM_PENDING_INTENT_REQUEST_CODE,
                Intent(app.applicationContext, QRAlarmService::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                app.applicationContext,
                ALARM_PENDING_INTENT_REQUEST_CODE,
                Intent(app.applicationContext, QRAlarmService::class.java),
                PendingIntent.FLAG_NO_CREATE or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
            )
        } != null

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val testingPermissionAlarmPendingIntent = PendingIntent.getForegroundService(
                app.applicationContext,
                TESTING_PERMISSION_ALARM_INTENT_REQUEST_CODE,
                Intent(app.applicationContext, QRAlarmService::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(
                        currentTimeInMillis() + 10000,
                        null
                    ),
                    testingPermissionAlarmPendingIntent
                )
            } catch (exception: SecurityException) {
                return false
            }

            alarmManager.cancel(testingPermissionAlarmPendingIntent)
            testingPermissionAlarmPendingIntent.cancel()

            return true
        } else {
            return true
        }
    }

    fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }
    }
}