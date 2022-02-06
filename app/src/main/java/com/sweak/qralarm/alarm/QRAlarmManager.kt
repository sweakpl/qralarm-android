package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.sweak.qralarm.MainActivity
import javax.inject.Inject

class QRAlarmManager @Inject constructor(
    private val alarmManager: AlarmManager,
    private val app: Application
) {
    fun setAlarm(alarmTimeInMillis: Long, alarmType: Int) {
        val intentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE
                else 0

        val alarmIntent = Intent(app.applicationContext, QRAlarmReceiver::class.java).apply {
            putExtra(QRAlarmService.ALARM_TYPE_KEY, alarmType)
        }

        val alarmPendingIntent = PendingIntent.getBroadcast(
            app.applicationContext,
            ALARM_PENDING_INTENT_REQUEST_CODE,
            alarmIntent,
            intentFlags
        )

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
    }

    fun cancelAlarm() {
        app.stopService(Intent(app.applicationContext, QRAlarmService::class.java))

        val pendingIntent = PendingIntent.getBroadcast(
            app.applicationContext,
            ALARM_PENDING_INTENT_REQUEST_CODE,
            Intent(app.applicationContext, QRAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    companion object {
        const val ALARM_PENDING_INTENT_REQUEST_CODE = 100
        const val ALARM_INFO_PENDING_INTENT_REQUEST_CODE = 101
    }
}