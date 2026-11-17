package com.sweak.qralarm.alarm.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.alarm.util.setAlarmVolume
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.HandleEarlyStoppedAlarm
import com.sweak.qralarm.core.domain.alarm.StartAlarmRinging
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var alarmsRepository: AlarmsRepository
    @Inject
    lateinit var qrAlarmManager: QRAlarmManager
    @Inject
    lateinit var handleEarlyStoppedAlarm: HandleEarlyStoppedAlarm
    @Inject
    lateinit var startAlarmRinging: StartAlarmRinging
    @Inject
    lateinit var alarmRingtonePlayer: AlarmRingtonePlayer
    @Inject
    lateinit var audioManager: AudioManager
    @Inject
    lateinit var userDataRepository: UserDataRepository
    @Inject
    lateinit var alarmDeliveryValidator: AlarmDeliveryValidator
    @Inject
    lateinit var alarmNotificationFactory: AlarmNotificationFactory

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
                    delay(muteDurationSeconds.seconds)
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
                    delay(EMERGENCY_TASK_ALARM_MUTE_DURATION_SECONDS.seconds)
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

        try {
            ServiceCompat.startForeground(
                this,
                alarmId.toInt(),
                alarmNotificationFactory.create(alarmId = alarmId),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else 0
            )
        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                exception is ForegroundServiceStartNotAllowedException
            ) {
                shouldStopService = true
            } else {
                throw exception
            }
        }

        serviceScope.launch {
            val isSnoozeAlarm = intent.extras?.getBoolean(EXTRA_IS_SNOOZE_ALARM)

            if (shouldStopService) {
                userDataRepository.setAlarmMissedDetected(detected = true)
                leaveForeground(alarmId)
                return@launch
            }

            val deliveredAlarm = alarmsRepository.getAlarm(alarmId = alarmId)
            deliveredAlarm?.let { alarm = it }

            val verdict = alarmDeliveryValidator.validate(
                alarm = deliveredAlarm,
                isSnoozeAlarm = isSnoozeAlarm
            )

            when (verdict) {
                AlarmDeliveryValidator.Verdict.StopEarly -> {
                    leaveForeground(alarmId)
                    handleEarlyStoppedAlarm(alarmId = alarmId)
                    return@launch
                }

                // An early delivery is waited out, so that the alarm rings on time and the
                // rescheduling below does not schedule the very same occurrence again:
                is AlarmDeliveryValidator.Verdict.Ring -> {
                    if (verdict.waitMillis > 0) delay(verdict.waitMillis.milliseconds)
                }
            }

            // The service is a singleton, so if a previous alarm is still ringing tear down first.
            if (isRunning) {
                withContext(Dispatchers.Main) {
                    cleanUpPreviousAlarmState()
                }
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

            startAlarmRinging(alarmId = alarmId, isSnoozeAlarm = isSnoozeAlarm)

            adjustAlarmVolume()

            withContext(Dispatchers.Main) {
                startAlarm()
            }
        }

        return START_NOT_STICKY
    }

    private fun leaveForeground(alarmId: Long) {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)
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

            audioManager.setAlarmVolume(volumeLevel)
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

    private fun cleanUpPreviousAlarmState() {
        temporaryAlarmMuteJob?.cancel()
        temporaryAlarmMuteJob = null
        emergencyTaskAlarmMuteJob?.cancel()
        emergencyTaskAlarmMuteJob = null
        hasAlarmBeenAlreadyTemporarilyMuted = false

        try {
            unregisterReceiver(temporaryAlarmMuteReceiver)
            unregisterReceiver(emergencyTaskAlarmMuteReceiver)
        } catch (_: IllegalArgumentException) { /* no-op */
        }

        alarmRingtonePlayer.stop()

        originalSystemAlarmVolume?.let {
            audioManager.setAlarmVolume(it)
        }
        originalSystemAlarmVolume = null
    }

    override fun onDestroy() {
        isRunning = false

        temporaryAlarmMuteJob?.cancel()
        emergencyTaskAlarmMuteJob?.cancel()

        try {
            unregisterReceiver(temporaryAlarmMuteReceiver)
            unregisterReceiver(emergencyTaskAlarmMuteReceiver)
        } catch (_: IllegalArgumentException) { /* no-op */
        }

        alarmRingtonePlayer.apply {
            stop()
            onDestroy()
        }

        originalSystemAlarmVolume?.let {
            audioManager.setAlarmVolume(it)
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