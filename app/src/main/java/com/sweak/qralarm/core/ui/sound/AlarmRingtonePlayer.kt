package com.sweak.qralarm.core.ui.sound

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RawRes
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmRingtonePlayer(
    private val context: Context,
    private val vibrator: Vibrator
) {
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var player: ExoPlayer? = null

    lateinit var volumeIncreaseJob: Job
    lateinit var vibrationDelayJob: Job

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
    }

    fun playAlarmRingtone(ringtone: Ringtone, volumeIncreaseSeconds: Int) {
        val alarmRingtoneUri: Uri = if (ringtone != Ringtone.CUSTOM_SOUND) {
            getOriginalAlarmRingtoneUri(ringtone)
        } else {
            getOriginalAlarmRingtoneUri(Ringtone.GENTLE_GUITAR)
        }

        playAlarmRingtone(alarmRingtoneUri, volumeIncreaseSeconds)
    }

    fun playAlarmRingtone(alarmRingtoneUri: Uri, volumeIncreaseSeconds: Int) {
        initializePlayer()

        player?.apply {
            setMediaItem(MediaItem.fromUri(alarmRingtoneUri))
            repeatMode = Player.REPEAT_MODE_ALL
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_ALARM)
                    .build(),
                false
            )
            playWhenReady = false
            prepare()
        }

        if (volumeIncreaseSeconds > 0) {
            volumeIncreaseJob = playerScope.launch {
                for (volume in 0..100) {
                    val scaledVolume = volume / 100f
                    player?.volume = scaledVolume

                    delay(volumeIncreaseSeconds * 10L)
                }
            }
        }

        player?.play()
    }

    fun playAlarmRingtonePreview(
        ringtone: Ringtone,
        onPreviewCompleted: (hasErrorOccurred: Boolean) -> Unit
    ) {
        if (ringtone == Ringtone.CUSTOM_SOUND) {
            onPreviewCompleted(true)
            return
        }

        playAlarmRingtonePreview(getOriginalAlarmRingtoneUri(ringtone), onPreviewCompleted)
    }

    fun playAlarmRingtonePreview(
        alarmRingtoneUri: Uri,
        onPreviewCompleted: (hasErrorOccurred: Boolean) -> Unit
    ) {
        initializePlayer()

        player?.apply {
            setMediaItem(MediaItem.fromUri(alarmRingtoneUri))
            repeatMode = Player.REPEAT_MODE_OFF
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        stop()
                        onPreviewCompleted(false)
                    }
                }
            })
            playWhenReady = true
            prepare()
        }
    }

    fun stop() {
        if (::volumeIncreaseJob.isInitialized) volumeIncreaseJob.cancel()
        if (::vibrationDelayJob.isInitialized) vibrationDelayJob.cancel()

        vibrator.cancel()

        player?.apply {
            stop()
            clearMediaItems()
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
            val vibrationAudioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
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
        player?.release()
        player = null
    }
}