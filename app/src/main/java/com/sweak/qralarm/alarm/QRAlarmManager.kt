package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.receiver.PostUpcomingAlarmNotificationReceiver
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.alarm.service.AlarmService.Companion.EXTRA_ALARM_ID
import com.sweak.qralarm.alarm.service.AlarmService.Companion.EXTRA_IS_SNOOZE_ALARM
import com.sweak.qralarm.app.MainActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta

class QRAlarmManager(
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManager,
    private val context: Context
) {
    fun setAlarm(alarmId: Long, alarmTimeInMillis: Long, isSnoozeAlarm: Boolean) {
        val alarmIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_SNOOZE_ALARM, isSnoozeAlarm)
        }

        val alarmPendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    alarmId.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    alarmId.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                )
            }

        val alarmInfoPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(alarmTimeInMillis, alarmInfoPendingIntent),
            alarmPendingIntent
        )
    }

    fun scheduleUpcomingAlarmNotification(
        alarmId: Long,
        upcomingAlarmNotificationTimeInMillis: Long
    ) {
        alarmManager.set(
            RTC_WAKEUP,
            upcomingAlarmNotificationTimeInMillis,
            PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                Intent(context, PostUpcomingAlarmNotificationReceiver::class.java).apply {
                    putExtra(PostUpcomingAlarmNotificationReceiver.EXTRA_ALARM_ID, alarmId)
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE
                else 0
            )
        )
    }

    fun cancelAlarm(alarmId: Long) {
        val alarmPendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    alarmId.toInt(),
                    Intent(context, AlarmService::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    alarmId.toInt(),
                    Intent(context, AlarmService::class.java),
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

        cancelUpcomingAlarmNotification(alarmId = alarmId)
    }

    fun cancelUpcomingAlarmNotification(alarmId: Long) {
        val upcomingAlarmNotificationPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            Intent(context, PostUpcomingAlarmNotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        if (upcomingAlarmNotificationPendingIntent != null) {
            alarmManager.cancel(upcomingAlarmNotificationPendingIntent)
            upcomingAlarmNotificationPendingIntent.cancel()
        }

        notificationManager.cancel(Int.MAX_VALUE - alarmId.toInt())
    }

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
        ) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }
    }

    fun notifyAboutMissedAlarm() {
        val alarmMissedPendingIntent = PendingIntent.getActivity(
            context,
            ALARM_MISSED_NOTIFICATION_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val contentText = context.getString(R.string.alarm_missed_notification_text)
        val alarmSetIndicationNotification = NotificationCompat.Builder(
            context,
            ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(false)
            setAutoCancel(true)
            setColorized(true)
            setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            setContentTitle(context.getString(R.string.alarm_missed_notification_title))
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_qralarm)
            setContentIntent(alarmMissedPendingIntent)
        }.build()

        notificationManager.notify(
            ALARM_MISSED_NOTIFICATION_ID,
            alarmSetIndicationNotification
        )
    }
}