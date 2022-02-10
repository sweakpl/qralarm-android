package com.sweak.qralarm.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.QRAlarmApp
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.Jacarta
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalPermissionsApi
@AndroidEntryPoint
class QRAlarmService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var vibrator: Vibrator

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(message: Message) {
            if (message.arg2 !in arrayOf(ALARM_TYPE_NORMAL, ALARM_TYPE_SNOOZE)) {
                stopSelf(message.arg1)
            } else {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        startForeground(
                            FOREGROUND_SERVICE_ID,
                            createAlarmNotification(),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        startForeground(
                            FOREGROUND_SERVICE_ID,
                            createAlarmNotification()
                        )
                    }
                    else -> {
                        notificationManager.notify(
                            ALARM_NOTIFICATION_ID,
                            createAlarmNotification()
                        )
                    }
                }

                startVibratingAndPlayingAlarmSound()
            }
        }
    }

    private fun createAlarmNotification(): Notification {
        val alarmNotificationPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ALARM_NOTIFICATION_REQUEST_CODE,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        NotificationCompat.Builder(
            applicationContext,
            QRAlarmApp.ALARM_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_HIGH
            setOngoing(true)
            setColorized(true)
            setContentTitle(getString(R.string.alarm_notification_title))
            setContentText(getString(R.string.alarm_notification_text))
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentIntent(alarmNotificationPendingIntent)
            return build()
        }
    }

    private fun startVibratingAndPlayingAlarmSound() {
        startVibrating()
        startPlayingAlarmSound()
    }

    private fun startVibrating() {
        val vibrationAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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

    private fun startPlayingAlarmSound() {
        mediaPlayer.apply {
            reset()
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(
                applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            isLooping = true
            prepare()
            start()
        }
    }

    override fun onCreate() {
        super.onCreate()

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
                intent?.getIntExtra(ALARM_TYPE_KEY, ALARM_TYPE_NORMAL) ?: ALARM_TYPE_NORMAL

            serviceHandler?.sendMessage(message)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopVibratingAndPlayingSound()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        } else {
            notificationManager.cancel(ALARM_NOTIFICATION_ID)
        }
    }

    private fun stopVibratingAndPlayingSound() {
        vibrator.cancel()

        try {
            mediaPlayer.stop()
        } catch (exception: IllegalStateException) {
            Log.e("QRAlarmService", "mediaPlayer was not initialized! Cannot stop it...")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val HANDLER_THREAD_NAME = "QRAlarmHandlerThread"
        const val FOREGROUND_SERVICE_ID = 300
        const val ALARM_NOTIFICATION_ID = 300
        const val ALARM_NOTIFICATION_REQUEST_CODE = 400

        const val ALARM_TYPE_KEY = "alarmTypeKey"
        const val ALARM_TYPE_NORMAL = 200
        const val ALARM_TYPE_SNOOZE = 201
        const val ALARM_TYPE_NONE = 202
    }
}