package com.sweak.qralarm.alarm

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class QRAlarmService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var vibrator: Vibrator

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private lateinit var vibrationTask: TimerTask
    private lateinit var alarmVolumeAnimator: ValueAnimator

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(message: Message) {
            if (message.arg2 !in arrayOf(ALARM_TYPE_NORMAL, ALARM_TYPE_SNOOZE)) {
                stopSelf(message.arg1)
            } else {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        startForeground(
                            FOREGROUND_SERVICE_ID,
                            createAlarmNotification(message.arg2),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        startForeground(
                            FOREGROUND_SERVICE_ID,
                            createAlarmNotification(message.arg2)
                        )
                    }
                    else -> {
                        notificationManager.notify(
                            ALARM_NOTIFICATION_ID,
                            createAlarmNotification(message.arg2)
                        )
                    }
                }

                val gentleWakeupDelaySeconds = runBlocking {
                    dataStoreManager.getInt(DataStoreManager.GENTLE_WAKEUP_DURATION_SECONDS).first()
                }

                startVibratingAndPlayingAlarmSound(
                    GentleWakeupDuration.fromInt(gentleWakeupDelaySeconds)
                )
            }
        }
    }

    private fun createAlarmNotification(alarmType: Int): Notification {
        val alarmNotificationPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ALARM_NOTIFICATION_REQUEST_CODE,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val alarmFullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ALARM_FULL_SCREEN_REQUEST_CODE,
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(LOCK_SCREEN_VISIBILITY_FLAG, true)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        NotificationCompat.Builder(
            applicationContext,
            ALARM_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_HIGH
            setOngoing(true)
            setColorized(true)
            setContentTitle(getString(R.string.alarm_notification_title))
            setContentText(
                if (alarmType == ALARM_TYPE_NORMAL)
                    getString(R.string.alarm_notification_text_normal)
                else
                    getString(R.string.alarm_notification_text_snooze)
            )
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentIntent(alarmNotificationPendingIntent)
            setFullScreenIntent(alarmFullScreenPendingIntent, true)
            return build()
        }
    }

    private fun startVibratingAndPlayingAlarmSound(gentleWakeupDuration: GentleWakeupDuration?) {
        val vibrationsEnabled = runBlocking {
            dataStoreManager.getBoolean(DataStoreManager.ENABLE_VIBRATIONS).first()
        }

        if (vibrationsEnabled) {
            startVibrating(gentleWakeupDuration)
        }

        startPlayingAlarmSound(gentleWakeupDuration)
    }

    private fun startVibrating(gentleWakeupDuration: GentleWakeupDuration?) {
        vibrationTask = timerTask {
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

                return@timerTask
            }

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

        if (gentleWakeupDuration != null &&
            gentleWakeupDuration != GentleWakeupDuration.GENTLE_WAKEUP_DURATION_0_SECONDS
        ) {
            Timer().schedule(vibrationTask, gentleWakeupDuration.inMillis())
        } else {
            vibrationTask.run()
        }
    }

    private fun startPlayingAlarmSound(gentleWakeupDuration: GentleWakeupDuration?) {
        val mediaPlayer = mediaPlayer.apply {
            reset()
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                setDataSource(
                    applicationContext,
                    getPreferredAlarmSoundUri()
                )
            } catch (ioException: IOException) {
                release()
                return@apply
            }
            isLooping = true
            prepare()
        }

        if (gentleWakeupDuration != null &&
            gentleWakeupDuration != GentleWakeupDuration.GENTLE_WAKEUP_DURATION_0_SECONDS
        ) {
            alarmVolumeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = gentleWakeupDuration.inMillis()
                interpolator = LinearInterpolator()
                addUpdateListener {
                    mediaPlayer.setVolume(it.animatedValue as Float, it.animatedValue as Float)
                }
                start()
            }
        }

        mediaPlayer.start()
    }

    private fun getPreferredAlarmSoundUri(): Uri {
        val alarmSoundOrdinal = runBlocking {
            dataStoreManager.getInt(DataStoreManager.ALARM_SOUND).first()
        }

        return if (alarmSoundOrdinal == AlarmSound.LOCAL_SOUND.ordinal) {
            runBlocking {
                Uri.parse(
                    dataStoreManager.getString(DataStoreManager.LOCAL_ALARM_SOUND_URI).first()
                )
            }
        } else {
            AlarmSound.fromInt(alarmSoundOrdinal).let {
                Uri.parse(
                    "android.resource://"
                            + packageName
                            + "/"
                            + (it?.resourceId ?: AlarmSound.GENTLE_GUITAR.resourceId)
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, true)
                putBoolean(DataStoreManager.ALARM_SERVICE_PROPERLY_CLOSED, false)
            }
        }

        HandlerThread(HANDLER_THREAD_NAME, Process.THREAD_PRIORITY_URGENT_AUDIO).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler?.obtainMessage()?.also { message ->
            message.arg1 = startId
            message.arg2 =
                intent?.getIntExtra(KEY_ALARM_TYPE, ALARM_TYPE_NORMAL) ?: ALARM_TYPE_NORMAL

            serviceHandler?.sendMessage(message)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, false)
                putBoolean(DataStoreManager.ALARM_SERVICE_PROPERLY_CLOSED, true)
            }
        }

        stopVibratingAndPlayingSound()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        } else {
            notificationManager.cancel(ALARM_NOTIFICATION_ID)
        }
    }

    private fun stopVibratingAndPlayingSound() {
        if (::vibrationTask.isInitialized) vibrationTask.cancel()
        if (::alarmVolumeAnimator.isInitialized) alarmVolumeAnimator.end()

        vibrator.cancel()

        try {
            mediaPlayer.stop()
        } catch (exception: IllegalStateException) {
            Log.e("QRAlarmService", "mediaPlayer was not initialized! Cannot stop it...")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}