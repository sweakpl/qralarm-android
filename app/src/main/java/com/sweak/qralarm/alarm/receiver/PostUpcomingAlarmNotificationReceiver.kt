package com.sweak.qralarm.alarm.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.UPCOMING_ALARM_NOTIFICATION_REQUEST_CODE
import com.sweak.qralarm.app.MainActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.getTimeString
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
    @Inject lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        receiverScope.launch {
            val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID) ?: return@launch

            if (alarmId == 0L) return@launch

            val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return@launch

            val upcomingAlarmIndicationPendingIntent = PendingIntent.getActivity(
                context,
                UPCOMING_ALARM_NOTIFICATION_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
            )

            val upcomingAlarmIndicationNotification = NotificationCompat.Builder(
                context,
                ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
            ).apply {
                color = Jacarta.toArgb()
                priority = NotificationCompat.PRIORITY_LOW
                setOngoing(true)
                setColorized(true)
                setContentTitle(
                    context.getString(R.string.upcoming_alarm_indication_notification_title)
                )
                setContentText(
                    context.getString(
                        R.string.upcoming_alarm_indication_notification_text,
                        getTimeString(
                            hourOfDay = alarm.alarmHourOfDay,
                            minute = alarm.alarmMinute,
                            is24HourFormat = DateFormat.is24HourFormat(context)
                        )
                    )
                )
                setSmallIcon(R.drawable.ic_qralarm)
                setContentIntent(upcomingAlarmIndicationPendingIntent)
            }.build()

            notificationManager.notify(
                Int.MAX_VALUE - alarmId.toInt(),
                upcomingAlarmIndicationNotification
            )
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}