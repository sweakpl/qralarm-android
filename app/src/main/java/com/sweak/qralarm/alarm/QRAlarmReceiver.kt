package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class QRAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmServiceIntent = Intent(context, QRAlarmService::class.java)

        intent?.apply {
            alarmServiceIntent.putExtra(
                QRAlarmService.ALARM_TYPE_KEY,
                getIntExtra(QRAlarmService.ALARM_TYPE_KEY, 202)
            )
        }

        context?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(alarmServiceIntent)
            } else {
                startService(alarmServiceIntent)
            }
        }
    }
}