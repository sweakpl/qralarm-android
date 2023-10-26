package com.sweak.qralarm.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.ALARM_MISSED_NOTIFICATION_ID
import com.sweak.qralarm.util.ALARM_MISSED_NOTIFICATION_REQUEST_CODE
import com.sweak.qralarm.util.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
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

    @Inject
    lateinit var notificationManager: NotificationManager

    private val intentActionsToFilter = listOf(
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_DATE_CHANGED
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) {
            runBlocking {
                val isAlarmSet = dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first()
                if (!isAlarmSet) return@runBlocking

                val isSnoozeAlarmSet =
                    dataStoreManager.getBoolean(DataStoreManager.ALARM_SNOOZED).first()

                val alarmTimeInMillis = dataStoreManager.getLong(
                    if (isSnoozeAlarmSet) {
                        DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS
                    } else {
                        DataStoreManager.ALARM_TIME_IN_MILLIS
                    }
                ).first()

                if (currentTimeInMillis() > alarmTimeInMillis) {
                    qrAlarmManager.cancelAlarm()

                    with(dataStoreManager) {
                        putBoolean(DataStoreManager.ALARM_SET, false)
                        putBoolean(DataStoreManager.ALARM_SNOOZED, false)
                    }

                    showAlarmMissedNotification(context)
                }
            }
        }
    }

    private fun showAlarmMissedNotification(context: Context) {
        val alarmMissedPendingIntent = PendingIntent.getActivity(
            context,
            ALARM_MISSED_NOTIFICATION_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val alarmSetIndicationNotification = NotificationCompat.Builder(
            context,
            ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(false)
            setAutoCancel(true)
            setColorized(true)
            setContentTitle(context.getString(R.string.alarm_missed_notification_title))
            setContentText(context.getString(R.string.alarm_missed_notification_text))
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentIntent(alarmMissedPendingIntent)
        }.build()

        notificationManager.notify(
            ALARM_MISSED_NOTIFICATION_ID,
            alarmSetIndicationNotification
        )
    }
}