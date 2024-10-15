package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.alarm.service.AlarmService.Companion.EXTRA_ALARM_ID
import com.sweak.qralarm.app.MainActivity
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import java.time.ZonedDateTime

class QRAlarmManager(
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManager,
    private val alarmsRepository: AlarmsRepository,
    private val context: Context
) {
    suspend fun setAlarm(alarmId: Long): Long? {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return null

        val alarmTimeInMillis = when (alarm.repeatingMode) {
            is Alarm.RepeatingMode.Once -> alarm.repeatingMode.alarmDayInMillis
            is Alarm.RepeatingMode.Days -> {
                val todayDateTime = ZonedDateTime.now()
                var alarmDateTime = ZonedDateTime.now()
                    .withHour(alarm.alarmHourOfDay)
                    .withMinute(alarm.alarmMinute)
                    .withSecond(0)
                    .withNano(0)

                while (alarmDateTime < todayDateTime ||
                    alarmDateTime.dayOfWeek !in alarm.repeatingMode.repeatingDaysOfWeek
                ) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmDateTime.toInstant().toEpochMilli()
            }
        }

        val alarmIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val alarmPendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    alarm.alarmId.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    alarm.alarmId.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                )
            }

        val alarmInfoPendingIntent = PendingIntent.getActivity(
            context,
            alarm.alarmId.toInt(),
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

        return alarmTimeInMillis
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
}