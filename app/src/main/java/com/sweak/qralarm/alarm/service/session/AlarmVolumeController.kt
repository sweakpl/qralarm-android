package com.sweak.qralarm.alarm.service.session

import android.media.AudioManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.sweak.qralarm.core.domain.alarm.Alarm
import javax.inject.Inject

class AlarmVolumeController @Inject constructor(
    private val audioManager: AudioManager
) {
    private var originalSystemAlarmVolume: Int? = null

    fun start(alarm: Alarm) {
        val alarmVolumeMode = alarm.alarmVolumeMode

        if (alarmVolumeMode is Alarm.AlarmVolumeMode.Custom) {
            applyCustomVolume(volumePercentage = alarmVolumeMode.volumePercentage)
        }
    }

    fun stop() {
        originalSystemAlarmVolume?.let {
            audioManager.setAlarmVolume(it)
        }
        originalSystemAlarmVolume = null
    }

    private fun applyCustomVolume(volumePercentage: Int) {
        originalSystemAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        val volumeLevel = customVolumeLevel(
            minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
            } else 0,
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            volumePercentage = volumePercentage
        )

        audioManager.setAlarmVolume(volumeLevel)
    }

    companion object {
        @VisibleForTesting
        internal fun customVolumeLevel(minVolume: Int, maxVolume: Int, volumePercentage: Int): Int {
            val volumeRange = maxVolume - minVolume

            return (minVolume + (volumeRange * (volumePercentage / 100.0))).toInt()
                .coerceAtLeast(minVolume + 1)
        }
    }
}
