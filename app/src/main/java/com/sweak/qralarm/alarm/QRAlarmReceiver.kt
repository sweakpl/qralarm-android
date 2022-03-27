package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.util.ALARM_TYPE_NONE
import com.sweak.qralarm.util.KEY_ALARM_TYPE
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
class QRAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmServiceIntent = Intent(context, QRAlarmService::class.java)

        intent?.apply {
            alarmServiceIntent.putExtra(
                KEY_ALARM_TYPE,
                getIntExtra(
                    KEY_ALARM_TYPE,
                    ALARM_TYPE_NONE
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