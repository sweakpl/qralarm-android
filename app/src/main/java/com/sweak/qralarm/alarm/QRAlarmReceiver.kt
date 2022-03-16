package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
class QRAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmServiceIntent = Intent(context, QRAlarmService::class.java)

        intent?.apply {
            alarmServiceIntent.putExtra(
                QRAlarmService.ALARM_TYPE_KEY,
                getIntExtra(
                    QRAlarmService.ALARM_TYPE_KEY,
                    QRAlarmService.ALARM_TYPE_NONE
                )
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