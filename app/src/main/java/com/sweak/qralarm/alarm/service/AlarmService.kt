package com.sweak.qralarm.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ACTION_TEMPORARY_ALARM_MUTE
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var qrAlarmManager: QRAlarmManager
    @Inject lateinit var disableAlarm: DisableAlarm
    @Inject lateinit var setAlarm: SetAlarm
    @Inject lateinit var alarmRingtonePlayer: AlarmRingtonePlayer

    private lateinit var alarm: Alarm

    private lateinit var temporaryAlarmMuteJob: Job
    private var hasAlarmBeenAlreadyTemporarilyMuted = false

    private val temporaryAlarmMuteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (hasAlarmBeenAlreadyTemporarilyMuted) return
            else hasAlarmBeenAlreadyTemporarilyMuted = true

            serviceScope.launch {
                alarmRingtonePlayer.stop()

                temporaryAlarmMuteJob = serviceScope.launch {
                    delay(15000) // 15 seconds
                    startAlarm()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var shouldStopService = false
        val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID).run {
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

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)

        serviceScope.launch {
            alarmsRepository.getAlarm(alarmId = alarmId)?.let {
                alarm = it
            } ?: run {
                ServiceCompat.stopForeground(
                    this@AlarmService,
                    ServiceCompat.STOP_FOREGROUND_REMOVE
                )
                return@launch
            }

            isRunning = true

            ContextCompat.registerReceiver(
                this@AlarmService,
                temporaryAlarmMuteReceiver,
                IntentFilter(ACTION_TEMPORARY_ALARM_MUTE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            intent.extras?.getBoolean(EXTRA_IS_SNOOZE_ALARM)?.let { isSnoozeAlarm ->
                if (!isSnoozeAlarm) {
                    resetAvailableSnoozes()
                }
            }

            alarmsRepository.setAlarmRunning(
                alarmId = alarmId,
                running = true
            )
            alarmsRepository.setAlarmSnoozed(
                alarmId = alarmId,
                snoozed = false
            )

            handleAlarmRescheduling()
            startAlarm()
        }

        return START_NOT_STICKY
    }

    private fun createAlarmNotification(alarmId: Long): Notification {
        val alarmNotificationPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarmId.toInt(),
            Intent(applicationContext, AlarmActivity::class.java).apply {
                putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, false)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
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
                putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, false)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
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

    private suspend fun resetAvailableSnoozes() {
        alarmsRepository.addOrEditAlarm(
            alarm = alarm.copy(
                snoozeConfig = alarm.snoozeConfig.copy(
                    numberOfSnoozesLeft = alarm.snoozeConfig.snoozeMode.numberOfSnoozes
                )
            )
        )
    }

    private suspend fun handleAlarmRescheduling() {
        if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
            disableAlarm(alarmId = alarm.alarmId)
        } else if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
            setAlarm(alarmId = alarm.alarmId)
        }
    }

    private fun startAlarm() {
        if (alarm.ringtone == Alarm.Ringtone.CUSTOM_SOUND) {
            if (alarm.customRingtoneUriString != null) {
                alarmRingtonePlayer.playAlarmRingtone(Uri.parse(alarm.customRingtoneUriString))
            } else {
                alarmRingtonePlayer.playAlarmRingtone(Alarm.Ringtone.GENTLE_GUITAR)
            }
        } else {
            alarmRingtonePlayer.playAlarmRingtone(alarm.ringtone)
        }

        if (alarm.areVibrationsEnabled) {
            alarmRingtonePlayer.startVibration()
        }
    }

    override fun onDestroy() {
        isRunning = false

        if (::temporaryAlarmMuteJob.isInitialized) temporaryAlarmMuteJob.cancel()
        unregisterReceiver(temporaryAlarmMuteReceiver)

        runBlocking {
            alarmsRepository.setAlarmRunning(
                alarmId = alarm.alarmId,
                running = false
            )
        }
        alarmRingtonePlayer.apply {
            stop()
            onDestroy()
        }
        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_IS_SNOOZE_ALARM = "isSnoozeAlarm"

        var isRunning = false
    }
}