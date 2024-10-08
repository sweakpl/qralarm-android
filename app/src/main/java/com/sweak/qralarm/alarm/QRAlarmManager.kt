package com.sweak.qralarm.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.os.Build

class QRAlarmManager(
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManager
) {
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