package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.util.currentTimeInMillis
import javax.inject.Inject

@ExperimentalPermissionsApi
class QRAlarmManager @Inject constructor(
    private val alarmManager: AlarmManager,
    private val packageManager: PackageManager,
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

        packageManager.setComponentEnabledSetting(
            ComponentName(app, BootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm() {
        app.stopService(Intent(app.applicationContext, QRAlarmService::class.java))
        removeAlarmPendingIntent()

        packageManager.setComponentEnabledSetting(
            ComponentName(app, BootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun removeAlarmPendingIntent() {
        val alarmPendingIntent = PendingIntent.getBroadcast(
            app.applicationContext,
            ALARM_PENDING_INTENT_REQUEST_CODE,
            Intent(app.applicationContext, QRAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent)
            alarmPendingIntent.cancel()
        }
    }

    fun isAlarmSet(): Boolean =
        PendingIntent.getBroadcast(
            app.applicationContext,
            ALARM_PENDING_INTENT_REQUEST_CODE,
            Intent(app.applicationContext, QRAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        ) != null

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val testingPermissionAlarmPendingIntent = PendingIntent.getBroadcast(
                app.applicationContext,
                TESTING_PERMISSION_ALARM_INTENT_REQUEST_CODE,
                Intent(app.applicationContext, QRAlarmReceiver::class.java),
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

    companion object {
        const val ALARM_PENDING_INTENT_REQUEST_CODE = 100
        const val ALARM_INFO_PENDING_INTENT_REQUEST_CODE = 101
        const val TESTING_PERMISSION_ALARM_INTENT_REQUEST_CODE = 102
    }
}