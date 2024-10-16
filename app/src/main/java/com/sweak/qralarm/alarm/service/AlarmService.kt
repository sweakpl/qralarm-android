package com.sweak.qralarm.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject lateinit var alarmsRepository: AlarmsRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("AlarmService", "onStartCommand")

        var shouldStopService = false
        val alarmId = intent?.extras?.getLong(EXTRA_ALARM_ID).run {
            if (this == null) {
                shouldStopService = true
                return@run 0
            } else {
                return@run this
            }
        }
        val alarmNotification = createAlarmNotification(alarmId = alarmId)

        ServiceCompat.startForeground(
            this,
            alarmId.toInt(),
            alarmNotification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else 0
        )

        runBlocking {
            Log.i("AlarmService", "Trying to read alarm of id $alarmId")
            val alarm = alarmsRepository.getAlarm(alarmId = alarmId)
            Log.i("AlarmService", alarm.toString())
        }

        if (shouldStopService) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    private fun createAlarmNotification(alarmId: Long): Notification {
        val alarmNotificationPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarmId.toInt(),
            Intent(applicationContext, AlarmActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val alarmFullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarmId.toInt(),
            Intent(applicationContext, AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        NotificationCompat.Builder(
            applicationContext,
            ALARM_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_HIGH
            setOngoing(true)
            setColorized(true)
            setContentTitle(getString(R.string.alarm_notification_title))
            setSmallIcon(R.drawable.ic_qralarm)
            setContentIntent(alarmNotificationPendingIntent)
            setFullScreenIntent(alarmFullScreenPendingIntent, true)
            return build()
        }
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}