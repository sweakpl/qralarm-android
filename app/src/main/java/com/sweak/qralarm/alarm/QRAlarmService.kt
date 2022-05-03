package com.sweak.qralarm.alarm

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
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalPagerApi
@InternalCoroutinesApi
@ExperimentalPermissionsApi
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

                startVibratingAndPlayingAlarmSound()
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
                getPreferredAlarmSoundUri()
            )
            isLooping = true
            prepare()
            start()
        }
    }

    private fun getPreferredAlarmSoundUri(): Uri {
        val alarmSoundOrdinal = runBlocking {
            dataStoreManager.getInt(DataStoreManager.ALARM_SOUND).first()
        }

        return AlarmSound.fromInt(alarmSoundOrdinal).let {
                Uri.parse(
                    "android.resource://"
                            + packageName
                            + "/"
                            + (it?.resourceId ?: AlarmSound.GENTLE_GUITAR.resourceId)
                )
        }
    }

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, true)
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
            dataStoreManager.putBoolean(DataStoreManager.ALARM_SERVICE_RUNNING, false)
        }

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
}