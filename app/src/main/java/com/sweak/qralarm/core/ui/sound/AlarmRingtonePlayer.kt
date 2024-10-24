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
import java.io.IOException

class AlarmRingtonePlayer(
    private val context: Context,
    private val mediaPlayer: MediaPlayer,
    private val vibrator: Vibrator
) {
    fun playAlarmRingtone(ringtone: Ringtone) {
        val alarmRingtoneUri: Uri = if (ringtone != Ringtone.CUSTOM_SOUND) {
            getOriginalAlarmRingtoneUri(ringtone)
        } else {
            getOriginalAlarmRingtoneUri(Ringtone.GENTLE_GUITAR)
        }

        playAlarmRingtone(alarmRingtoneUri)
    }

    fun playAlarmRingtone(alarmRingtoneUri: Uri) {
        mediaPlayer.apply {
            reset()
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, alarmRingtoneUri)
                isLooping = true
                prepare()
                start()
            } catch (illegalStateException: IllegalStateException) {
                return
            } catch (ioException: IOException) {
                return
            }
        }
    }

    fun playAlarmRingtonePreview(ringtone: Ringtone, onPreviewCompleted: () -> Unit) {
        val alarmRingtoneUri: Uri

        if (ringtone != Ringtone.CUSTOM_SOUND) {
            alarmRingtoneUri = getOriginalAlarmRingtoneUri(ringtone)
        } else {
            onPreviewCompleted()
            return
        }

        playAlarmRingtonePreview(alarmRingtoneUri, onPreviewCompleted)
    }

    fun playAlarmRingtonePreview(alarmRingtoneUri: Uri, onPreviewCompleted: () -> Unit) {
        mediaPlayer.apply {
            reset()
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                setDataSource(context, alarmRingtoneUri)
            } catch (ioException: IOException) {
                return
            }
            isLooping = false
            setOnCompletionListener {
                this@AlarmRingtonePlayer.stop()
                onPreviewCompleted()
            }
            prepare()
            start()
        }
    }

    fun stop() {
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

    @Suppress("DEPRECATION")
    fun startVibration() {
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
            Ringtone.ALARM_CLOCK -> R.raw.alarm_clock
            Ringtone.AIR_HORN -> R.raw.air_horn
            Ringtone.CUSTOM_SOUND -> -1
        }
    }

    fun onDestroy() {
        vibrator.cancel()
        mediaPlayer.apply {
            reset()
            release()
        }
    }
}