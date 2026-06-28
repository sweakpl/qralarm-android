package com.sweak.qralarm.core.ui.sound

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RawRes
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SET_VOLUME
import androidx.media3.exoplayer.ExoPlayer
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AlarmRingtonePlayer(
    private val context: Context,
    private val vibrator: Vibrator
) {
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var player: ExoPlayer? = null

    lateinit var volumeIncreaseJob: Job
    lateinit var vibrationJob: Job

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
    }

    fun isSystemDefaultAlarmRingtoneAvailable(): Boolean =
        getSystemDefaultAlarmRingtoneUri() != null

    fun playAlarmRingtone(ringtone: Ringtone, volumeIncreaseSeconds: Int) {
        val alarmRingtoneUri: Uri = when (ringtone) {
            Ringtone.CUSTOM_SOUND -> getOriginalAlarmRingtoneUri(Ringtone.GENTLE_GUITAR)
            Ringtone.SYSTEM_DEFAULT ->
                getSystemDefaultAlarmRingtoneUri()
                    ?: getOriginalAlarmRingtoneUri(Ringtone.GENTLE_GUITAR)

            else -> getOriginalAlarmRingtoneUri(ringtone)
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

        if (player?.isCommandAvailable(COMMAND_SET_VOLUME) == true) {
            if (::volumeIncreaseJob.isInitialized) volumeIncreaseJob.cancel()

            if (volumeIncreaseSeconds > 0) {
                player?.volume = 0f

                val totalMilliseconds = volumeIncreaseSeconds * 1000.0
                val steps = 100
                val perStepDelayMilliseconds = (totalMilliseconds / steps).toLong()
                val power = 2.0 // quadratic ease-in

                volumeIncreaseJob = playerScope.launch {
                    for (i in 0..steps) {
                        val progress = i.toDouble() / steps
                        val scaledVolume = progress.pow(power).toFloat()
                        player?.volume = scaledVolume
                        if (i < steps) delay(perStepDelayMilliseconds.milliseconds)
                    }
                }
            } else {
                player?.volume = 1f
            }
        }

        player?.play()
    }

    fun playAlarmRingtonePreview(
        ringtone: Ringtone,
        onPreviewCompleted: (hasErrorOccurred: Boolean) -> Unit
    ) {
        when (ringtone) {
            Ringtone.CUSTOM_SOUND -> {
                onPreviewCompleted(true)
                return
            }

            Ringtone.SYSTEM_DEFAULT -> {
                val systemDefaultUri = getSystemDefaultAlarmRingtoneUri()

                if (systemDefaultUri == null) {
                    onPreviewCompleted(true)
                    return
                }

                playAlarmRingtonePreview(systemDefaultUri, onPreviewCompleted)
            }

            else -> playAlarmRingtonePreview(
                getOriginalAlarmRingtoneUri(ringtone),
                onPreviewCompleted
            )
        }
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
        if (::vibrationJob.isInitialized) vibrationJob.cancel()

        vibrator.cancel()

        player?.apply {
            stop()
            clearMediaItems()
        }
    }

    fun startVibration(delaySeconds: Int) {
        if (::vibrationJob.isInitialized) vibrationJob.cancel()

        vibrationJob = playerScope.launch {
            if (delaySeconds > 0) {
                delay(delaySeconds.seconds)
            }

            while (isActive) {
                startVibrationInternal()
                delay(2.seconds)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibrationInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(1000, 1000),
                intArrayOf(255, 0),
                -1
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val vibrationAttributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_ALARM)
                    .build()

                vibrator.vibrate(vibrationEffect, vibrationAttributes)
            } else {
                val vibrationAudioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build()
                vibrator.vibrate(vibrationEffect, vibrationAudioAttributes)
            }
        } else {
            val vibrationAudioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            vibrator.vibrate(longArrayOf(0, 1000, 1000), -1, vibrationAudioAttributes)
        }
    }

    private fun getOriginalAlarmRingtoneUri(ringtone: Ringtone): Uri {
        return ("android.resource://"
                + context.packageName
                + "/"
                + getOriginalRingtoneResourceId(ringtone)).toUri()
    }

    private fun getSystemDefaultAlarmRingtoneUri(): Uri? {
        return try {
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        } catch (_: Exception) {
            null
        }
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
            Ringtone.SYSTEM_DEFAULT, Ringtone.CUSTOM_SOUND -> -1
        }
    }

    fun onDestroy() {
        playerScope.cancel()
        vibrator.cancel()
        player?.release()
        player = null
    }
}
