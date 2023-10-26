package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.currentTimeInMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class TimePreferencesChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    private val intentActionsToFilter = listOf(
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_DATE_CHANGED
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) {
            runBlocking {
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

                if (currentTimeInMillis() > alarmTimeInMillis) {
                    qrAlarmManager.cancelAlarm()

                    with(dataStoreManager) {
                        putBoolean(DataStoreManager.ALARM_SET, false)
                        putBoolean(DataStoreManager.ALARM_SNOOZED, false)
                    }
                }
            }
        }
    }
}