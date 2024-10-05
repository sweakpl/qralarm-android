package com.sweak.qralarm.core.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.AlarmRingtone
import java.io.IOException

class AlarmRingtonePlayer(
    private val context: Context,
    private val mediaPlayer: MediaPlayer
) {
    fun playOriginalAlarmRingtonePreview(
        alarmRingtone: AlarmRingtone,
        onPreviewCompleted: () -> Unit
    ) {
        mediaPlayer.apply {
            reset()
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                if (alarmRingtone != AlarmRingtone.CUSTOM_SOUND) {
                    setDataSource(context, getOriginalAlarmRingtoneUri(alarmRingtone))
                } else {
                    onPreviewCompleted()
                    return
                }
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

    private fun getOriginalAlarmRingtoneUri(alarmRingtone: AlarmRingtone): Uri {
        return Uri.parse(
            "android.resource://"
                    + context.packageName
                    + "/"
                    + getOriginalRingtoneResourceId(alarmRingtone)
        )
    }

    @RawRes
    private fun getOriginalRingtoneResourceId(alarmRingtone: AlarmRingtone): Int {
        return when (alarmRingtone) {
            AlarmRingtone.GENTLE_GUITAR -> R.raw.gentle_guitar
            AlarmRingtone.ALARM_CLOCK -> R.raw.alarm_clock
            AlarmRingtone.AIR_HORN -> R.raw.air_horn
            AlarmRingtone.CUSTOM_SOUND -> -1
        }
    }

    fun onDestroy() {
        mediaPlayer.apply {
            reset()
            release()
        }
    }
}