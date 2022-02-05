package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sweak.qralarm.MainActivity
import javax.inject.Inject

class QRAlarmManager @Inject constructor(
    private val alarmManager: AlarmManager,
    private val app: Application
) {
    fun setAlarm(alarmTimeInMillis: Long) {
        val intentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE
                else 0

        val alarmPendingIntent = PendingIntent.getBroadcast(
            app.applicationContext,
            ALARM_PENDING_INTENT_REQUEST_CODE,
            Intent(app.applicationContext, QRAlarmReceiver::class.java),
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

        Log.i("QRAlarmManager", "Alarm has been set!")
    }

    fun cancelAlarm() {
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

        Log.i("QRAlarmManager", "Alarm has been canceled!")
    }

    companion object {
        const val ALARM_PENDING_INTENT_REQUEST_CODE = 100
        const val ALARM_INFO_PENDING_INTENT_REQUEST_CODE = 101
    }
}