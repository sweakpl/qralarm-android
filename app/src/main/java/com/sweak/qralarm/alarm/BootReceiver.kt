package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.currentTimeInMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@InternalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var alarmManager: QRAlarmManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val shouldSetAlarm = runBlocking {
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first()
            }

            val isAlarmSet = alarmManager.isAlarmSet()

            if (shouldSetAlarm && !isAlarmSet) {
                val isSnoozeAlarmSet = runBlocking {
                    dataStoreManager.getBoolean(DataStoreManager.ALARM_SNOOZED).first()
                }

                val alarmTimeInMillis = runBlocking {
                    dataStoreManager.getLong(
                        if (isSnoozeAlarmSet) {
                            DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS
                        } else {
                            DataStoreManager.ALARM_TIME_IN_MILLIS
                        }
                    ).first()
                }

                val alarmType = if (isSnoozeAlarmSet) {
                    QRAlarmService.ALARM_TYPE_NORMAL
                } else {
                    QRAlarmService.ALARM_TYPE_SNOOZE
                }

                if (currentTimeInMillis() < alarmTimeInMillis) {
                    alarmManager.setAlarm(alarmTimeInMillis, alarmType)
                }
            }
        }
    }
}