package com.sweak.qralarm.alarm.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.core.net.toUri
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.HandleEarlyStoppedAlarm
import com.sweak.qralarm.core.domain.alarm.StartAlarmRinging
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
    lateinit var userDataRepository: UserDataRepository
    @Inject
    lateinit var alarmDeliveryValidator: AlarmDeliveryValidator
    @Inject
    lateinit var alarmNotificationFactory: AlarmNotificationFactory
    @Inject
    lateinit var alarmMuteController: AlarmMuteController
    @Inject
    lateinit var alarmVolumeController: AlarmVolumeController

    private lateinit var alarm: Alarm

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

            alarmMuteController.start(
                stopRinging = { alarmRingtonePlayer.stop() },
                resumeRinging = { withContext(Dispatchers.Main) { startAlarm() } }
            )

            startAlarmRinging(alarmId = alarmId, isSnoozeAlarm = isSnoozeAlarm)

            alarmVolumeController.start(alarm = alarm)

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
        alarmMuteController.stop()
        alarmRingtonePlayer.stop()
        alarmVolumeController.stop()
    }

    override fun onDestroy() {
        isRunning = false

        alarmMuteController.stop()

        alarmRingtonePlayer.apply {
            stop()
            onDestroy()
        }

        alarmVolumeController.stop()

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