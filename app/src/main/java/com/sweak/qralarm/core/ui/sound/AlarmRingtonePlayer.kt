package com.sweak.qralarm.core.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RawRes
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class AlarmRingtonePlayer(
    private val context: Context,
    private val mediaPlayer: MediaPlayer,
    private val vibrator: Vibrator
) {
    private val playerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    lateinit var volumeIncreaseJob: Job
    lateinit var vibrationDelayJob: Job

    fun playAlarmRingtone(ringtone: Ringtone, volumeIncreaseSeconds: Int) {
        val alarmRingtoneUri: Uri = if (ringtone != Ringtone.CUSTOM_SOUND) {
            getOriginalAlarmRingtoneUri(ringtone)
        } else {
            getOriginalAlarmRingtoneUri(Ringtone.GENTLE_GUITAR)
        }

        playAlarmRingtone(alarmRingtoneUri, volumeIncreaseSeconds)
    }

    fun playAlarmRingtone(alarmRingtoneUri: Uri, volumeIncreaseSeconds: Int) {
        mediaPlayer.apply {
            try {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, alarmRingtoneUri)
                isLooping = true
                prepare()
            } catch (illegalStateException: IllegalStateException) {
                return
            } catch (ioException: IOException) {
                return
            }
        }

        if (volumeIncreaseSeconds > 0) {
            volumeIncreaseJob = playerScope.launch {
                for (volume in 0..100) {
                    try {
                        val scaledVolume = volume / 100f
                        mediaPlayer.setVolume(scaledVolume, scaledVolume)
                    } catch (illegalStateException: IllegalStateException) {
                        Log.e(
                            "AlarmRingtonePlayer",
                            "mediaPlayer was not initialized! Cannot set volume..."
                        )
                    }

                    delay(volumeIncreaseSeconds * 10L)
                }
            }
        }

        mediaPlayer.start()
    }

    fun playAlarmRingtonePreview(
        ringtone: Ringtone,
        onPreviewCompleted: (hasErrorOccurred: Boolean) -> Unit
    ) {
        val alarmRingtoneUri: Uri

        if (ringtone != Ringtone.CUSTOM_SOUND) {
            alarmRingtoneUri = getOriginalAlarmRingtoneUri(ringtone)
        } else {
            onPreviewCompleted(true)
            return
        }

        playAlarmRingtonePreview(alarmRingtoneUri, onPreviewCompleted)
    }

    fun playAlarmRingtonePreview(
        alarmRingtoneUri: Uri,
        onPreviewCompleted: (hasErrorOccurred: Boolean) -> Unit
    ) {
        mediaPlayer.apply {
            try {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, alarmRingtoneUri)
                isLooping = false
                setOnCompletionListener {
                    this@AlarmRingtonePlayer.stop()
                    onPreviewCompleted(false)
                }
                prepare()
                start()
            } catch (ioException: IOException) {
                onPreviewCompleted(true)
                return
            } catch (illegalStateException: IllegalStateException) {
                onPreviewCompleted(true)
                return
            }
        }
    }

    fun stop() {
        if (::volumeIncreaseJob.isInitialized) volumeIncreaseJob.cancel()
        if (::vibrationDelayJob.isInitialized) vibrationDelayJob.cancel()

        try {
            vibrator.cancel()

            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (exception: IllegalStateException) {
            Log.e(
                "AlarmRingtonePlayer",
                "mediaPlayer was not initialized! Cannot stop it..."
            )
        }
    }

    fun startVibration(delaySeconds: Int) {
        if (delaySeconds > 0) {
            vibrationDelayJob = playerScope.launch {
                delay(delaySeconds * 1000L)
                startVibrationInternal()
            }
        } else {
            startVibrationInternal()
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibrationInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibrationAttributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_ALARM)
                .build()

            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(1000, 1000),
                intArrayOf(255, 0),
                0
            )

            vibrator.vibrate(vibrationEffect, vibrationAttributes)
        } else {
            val vibrationAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(1000, 1000),
                    intArrayOf(255, 0),
                    0
                )

                vibrator.vibrate(vibrationEffect, vibrationAudioAttributes)
            } else {
                vibrator.vibrate(longArrayOf(0, 1000, 1000), 0, vibrationAudioAttributes)
            }
        }
    }

    private fun getOriginalAlarmRingtoneUri(ringtone: Ringtone): Uri {
        return Uri.parse(
            "android.resource://"
                    + context.packageName
                    + "/"
                    + getOriginalRingtoneResourceId(ringtone)
        )
    }

    @RawRes
    private fun getOriginalRingtoneResourceId(ringtone: Ringtone): Int {
        return when (ringtone) {
            Ringtone.GENTLE_GUITAR -> R.raw.gentle_guitar
            Ringtone.KALIMBA -> R.raw.kalimba
            Ringtone.CLASSIC_ALARM -> R.raw.classic_alarm
            Ringtone.ALARM_CLOCK -> R.raw.alarm_clock
            Ringtone.ROOSTER -> R.raw.rooster
            Ringtone.AIR_HORN -> R.raw.air_horn
            Ringtone.CUSTOM_SOUND -> -1
        }
    }

    fun onDestroy() {
        playerScope.cancel()
        vibrator.cancel()
        mediaPlayer.apply {
            reset()
            release()
        }
    }
}