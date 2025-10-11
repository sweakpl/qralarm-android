package com.sweak.qralarm.alarm.service

import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var qrAlarmManager: QRAlarmManager
    @Inject lateinit var disableAlarm: DisableAlarm
    @Inject lateinit var setAlarm: SetAlarm
    @Inject lateinit var alarmRingtonePlayer: AlarmRingtonePlayer
    @Inject lateinit var audioManager: AudioManager
    @Inject lateinit var userDataRepository: UserDataRepository

    private lateinit var alarm: Alarm

    private var temporaryAlarmMuteJob: Job? = null
    private var emergencyTaskAlarmMuteJob: Job? = null
    private var hasAlarmBeenAlreadyTemporarilyMuted = false
    private var originalSystemAlarmVolume: Int? = null

    private val temporaryAlarmMuteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (hasAlarmBeenAlreadyTemporarilyMuted) {
                return
            } else {
                hasAlarmBeenAlreadyTemporarilyMuted = true
                emergencyTaskAlarmMuteJob?.cancel()
            }

            serviceScope.launch(Dispatchers.Main) {
                alarmRingtonePlayer.stop()

                val muteDurationSeconds = intent?.getIntExtra(
                    EXTRA_TEMPORARY_MUTE_DURATION_SECONDS,
                    DEFAULT_TEMPORARY_MUTE_DURATION_SECONDS
                ) ?: DEFAULT_TEMPORARY_MUTE_DURATION_SECONDS

                temporaryAlarmMuteJob = serviceScope.launch(Dispatchers.Main) {
                    delay(muteDurationSeconds * 1000L)
                    startAlarm()
                }.also {
                    it.invokeOnCompletion { temporaryAlarmMuteJob = null }
                }
            }
        }
    }

    private val emergencyTaskAlarmMuteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            temporaryAlarmMuteJob?.cancel()
            emergencyTaskAlarmMuteJob?.cancel()

            serviceScope.launch(Dispatchers.Main) {
                alarmRingtonePlayer.stop()

                emergencyTaskAlarmMuteJob = serviceScope.launch(Dispatchers.Main) {
                    delay(EMERGENCY_TASK_ALARM_MUTE_DURATION_SECONDS * 1000L)
                    startAlarm()
                }.also {
                    it.invokeOnCompletion { emergencyTaskAlarmMuteJob = null }
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

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)

        try {
            ServiceCompat.startForeground(
                this,
                alarmId.toInt(),
                createAlarmNotification(alarmId = alarmId),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else 0
            )
        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                exception is ForegroundServiceStartNotAllowedException
            ) {
                runBlocking { userDataRepository.setAlarmMissedDetected(detected = true) }
                shouldStopService = true
            } else {
                throw exception
            }
        }

        if (shouldStopService) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            return START_NOT_STICKY
        }

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

            ContextCompat.registerReceiver(
                this@AlarmService,
                emergencyTaskAlarmMuteReceiver,
                IntentFilter(ACTION_EMERGENCY_TASK_ALARM_MUTE),
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

            adjustAlarmVolume()

            withContext(Dispatchers.Main) {
                startAlarm()
            }
        }

        return START_NOT_STICKY
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun createAlarmNotification(alarmId: Long): Notification {
        val alarmNotificationPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarmId.toInt(),
            Intent(applicationContext, AlarmActivity::class.java).apply {
                putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, false)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmFullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarmId.toInt(),
            Intent(applicationContext, AlarmActivity::class.java).apply {
                putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, false)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationCompat.Builder(
            applicationContext,
            ALARM_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_MAX
            setCategory(NotificationCompat.CATEGORY_ALARM)
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
            setAlarm(
                alarmId = alarm.alarmId,
                isReschedulingMissedAlarm = false
            )
        }
    }

    private fun adjustAlarmVolume() {
        val alarmVolumeMode = alarm.alarmVolumeMode

        if (alarmVolumeMode is Alarm.AlarmVolumeMode.Custom) {
            originalSystemAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
            } else 0
            val volumeRange = maxVolume - minVolume
            val volumePercentage = alarmVolumeMode.volumePercentage
            val volumeLevel = (minVolume + (volumeRange * (volumePercentage / 100.0))).toInt()
                .coerceAtLeast(minVolume + 1)

            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeLevel, 0)
        }
    }

    private fun startAlarm() {
        if (alarm.ringtone == Alarm.Ringtone.CUSTOM_SOUND) {
            if (alarm.customRingtoneUriString != null) {
                alarmRingtonePlayer.playAlarmRingtone(
                    alarmRingtoneUri = alarm.customRingtoneUriString!!.toUri(),
                    volumeIncreaseSeconds = alarm.gentleWakeUpDurationInSeconds
                )
            } else {
                alarmRingtonePlayer.playAlarmRingtone(
                    ringtone = Alarm.Ringtone.GENTLE_GUITAR,
                    volumeIncreaseSeconds = alarm.gentleWakeUpDurationInSeconds
                )
            }
        } else {
            alarmRingtonePlayer.playAlarmRingtone(
                ringtone = alarm.ringtone,
                volumeIncreaseSeconds = alarm.gentleWakeUpDurationInSeconds
            )
        }

        if (alarm.areVibrationsEnabled) {
            alarmRingtonePlayer.startVibration(alarm.gentleWakeUpDurationInSeconds)
        }
    }

    override fun onDestroy() {
        isRunning = false

        if (::alarm.isInitialized) {
            serviceScope.launch {
                alarmsRepository.setAlarmRunning(
                    alarmId = alarm.alarmId,
                    running = false
                )
            }
        }

        temporaryAlarmMuteJob?.cancel()
        emergencyTaskAlarmMuteJob?.cancel()

        try {
            unregisterReceiver(temporaryAlarmMuteReceiver)
            unregisterReceiver(emergencyTaskAlarmMuteReceiver)
        } catch (_: IllegalArgumentException) { /* no-op */ }

        alarmRingtonePlayer.apply {
            stop()
            onDestroy()
        }

        originalSystemAlarmVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
        }

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_IS_SNOOZE_ALARM = "isSnoozeAlarm"

        const val ACTION_TEMPORARY_ALARM_MUTE = "com.sweak.qralarm.TEMPORARY_ALARM_MUTE"
        const val EXTRA_TEMPORARY_MUTE_DURATION_SECONDS = "muteDurationSeconds"
        const val DEFAULT_TEMPORARY_MUTE_DURATION_SECONDS = 15

        const val ACTION_EMERGENCY_TASK_ALARM_MUTE = "com.sweak.qralarm.EMERGENCY_TASK_ALARM_MUTE"
        const val EMERGENCY_TASK_ALARM_MUTE_DURATION_SECONDS = 10

        var isRunning = false
    }
}