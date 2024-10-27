package com.sweak.qralarm.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReschedulingReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var rescheduleAlarms: RescheduleAlarms

    private val intentActionsToFilter = listOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.LOCKED_BOOT_COMPLETED",
        "android.intent.action.ACTION_BOOT_COMPLETED",
        "android.intent.action.REBOOT",
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.MY_PACKAGE_REPLACED",
        "android.intent.action.TIME_SET",
        "android.intent.action.DATE_CHANGED",
        "android.intent.action.TIMEZONE_CHANGED"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) receiverScope.launch {
            rescheduleAlarms()
        }
    }
}