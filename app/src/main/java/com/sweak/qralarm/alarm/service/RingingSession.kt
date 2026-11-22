package com.sweak.qralarm.alarm.service

import androidx.annotation.MainThread
import androidx.core.net.toUri
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import javax.inject.Inject
import javax.inject.Provider

@MainThread
class RingingSession private constructor(
    val alarm: Alarm,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val alarmMuteController: AlarmMuteController,
    private val alarmVolumeController: AlarmVolumeController
) {
    private var isStopped = false

    fun start() {
        alarmMuteController.start(
            stopRinging = { alarmRingtonePlayer.stop() },
            resumeRinging = { play() }
        )

        alarmVolumeController.start(alarm = alarm)

        play()
    }

    fun stop() {
        if (isStopped) return
        isStopped = true

        alarmMuteController.stop()

        alarmRingtonePlayer.apply {
            stop()
            onDestroy()
        }

        alarmVolumeController.stop()
    }

    private fun play() {
        if (isStopped) return

        if (alarm.ringtone == Alarm.Ringtone.CUSTOM_SOUND) {
            if (alarm.customRingtoneUriString != null) {
                alarmRingtonePlayer.playAlarmRingtone(
                    alarmRingtoneUri = alarm.customRingtoneUriString.toUri(),
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

    class Factory @Inject constructor(
        private val alarmRingtonePlayerProvider: Provider<AlarmRingtonePlayer>,
        private val alarmMuteController: AlarmMuteController,
        private val alarmVolumeController: AlarmVolumeController
    ) {
        fun create(alarm: Alarm) = RingingSession(
            alarm = alarm,
            alarmRingtonePlayer = alarmRingtonePlayerProvider.get(),
            alarmMuteController = alarmMuteController,
            alarmVolumeController = alarmVolumeController
        )
    }
}
