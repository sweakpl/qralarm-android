package com.sweak.qralarm.core.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import java.io.IOException

class AlarmRingtonePlayer(
    private val context: Context,
    private val mediaPlayer: MediaPlayer
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
        mediaPlayer.apply {
            reset()
            release()
        }
    }
}