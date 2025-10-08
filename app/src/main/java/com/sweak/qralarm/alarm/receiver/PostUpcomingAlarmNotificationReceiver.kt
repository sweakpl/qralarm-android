package com.sweak.qralarm.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostUpcomingAlarmNotificationReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var qrAlarmManager: QRAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        receiverScope.launch {
            val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID) ?: return@launch

            if (alarmId == 0L) return@launch

            val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return@launch

            qrAlarmManager.showUpcomingAlarmNotification(
                alarmId = alarm.alarmId,
                alarmHourOfDay = alarm.alarmHourOfDay,
                alarmMinute = alarm.alarmMinute,
                isSnoozeAlarm = false
            )
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}