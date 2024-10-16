package com.sweak.qralarm.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootAndUpdateReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    @Inject lateinit var alarmsRepository: AlarmsRepository

    @Inject lateinit var qrAlarmManager: QRAlarmManager

    private val intentActionsToFilter = listOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.LOCKED_BOOT_COMPLETED",
        "android.intent.action.ACTION_BOOT_COMPLETED",
        "android.intent.action.REBOOT",
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.MY_PACKAGE_REPLACED"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) receiverScope.launch {
            Log.i("BootAndUpdateReceive", intent.action.toString())
            if (!qrAlarmManager.canScheduleExactAlarms()) {
                alarmsRepository.getAllAlarms().first().forEach { alarm ->
                    qrAlarmManager.cancelAlarm(alarmId = alarm.alarmId)
                    alarmsRepository.setAlarmEnabled(
                        alarmId = alarm.alarmId,
                        enabled = false
                    )
                }
            } else {
                alarmsRepository.getAllAlarms().first().forEach { alarm ->
                    if (alarm.isAlarmEnabled) {
                        qrAlarmManager.setAlarm(alarmId = alarm.alarmId)
                    }
                }
            }
        }
    }
}