package com.sweak.qralarm.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var disableAlarm: DisableAlarm
    @Inject lateinit var setAlarm: SetAlarm

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        if (shouldStopService) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            return START_NOT_STICKY
        }

        serviceScope.launch {
            alarmsRepository.getAlarm(alarmId = alarmId)?.let { alarm ->
                if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                    disableAlarm(alarmId = alarmId)
                } else if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                    setAlarm(alarmId = alarmId)
                }
            }
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
                putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
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

    override fun onDestroy() {
        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}