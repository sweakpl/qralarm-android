package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.ALARM_TYPE_NORMAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostUpcomingNotificationBroadcastReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        receiverScope.launch {
            val alarmTimeInMillis =
                dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()

            qrAlarmManager.postUpcomingAlarmIndicationNotification(
                alarmTimeInMillis,
                ALARM_TYPE_NORMAL
            )
        }
    }
}