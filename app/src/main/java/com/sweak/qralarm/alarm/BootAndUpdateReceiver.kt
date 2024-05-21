package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.ALARM_TYPE_NORMAL
import com.sweak.qralarm.util.ALARM_TYPE_SNOOZE
import com.sweak.qralarm.util.currentTimeInMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class BootAndUpdateReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var alarmManager: QRAlarmManager

    private val intentActionsToFilter = listOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.ACTION_BOOT_COMPLETED",
        "android.intent.action.REBOOT",
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.MY_PACKAGE_REPLACED"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) receiverScope.launch {
            val shouldSetAlarm = dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first()
            val isAlarmSet = alarmManager.isAlarmSet()

            if (shouldSetAlarm) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    dataStoreManager.apply {
                        putBoolean(DataStoreManager.ALARM_SET, false)
                        putBoolean(DataStoreManager.ALARM_SNOOZED, false)
                    }
                    return@launch
                }

                val isSnoozeAlarmSet =
                    dataStoreManager.getBoolean(DataStoreManager.ALARM_SNOOZED).first()

                val alarmTimeInMillis = dataStoreManager.getLong(
                    if (isSnoozeAlarmSet) {
                        DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS
                    } else {
                        DataStoreManager.ALARM_TIME_IN_MILLIS
                    }
                ).first()

                val alarmType = if (isSnoozeAlarmSet) ALARM_TYPE_SNOOZE else ALARM_TYPE_NORMAL

                if (currentTimeInMillis() < alarmTimeInMillis) {
                    if (!isAlarmSet) {
                        alarmManager.setAlarm(alarmTimeInMillis, alarmType)
                    } else {
                        alarmManager.postAlarmSetIndicationNotification(
                            alarmTimeInMillis,
                            alarmType
                        )
                    }

                    val alarmTimeZoneId = ZoneId.systemDefault().id
                    dataStoreManager.putString(DataStoreManager.ALARM_TIME_ZONE_ID, alarmTimeZoneId)
                }
            }
        }
    }
}