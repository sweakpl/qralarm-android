package com.sweak.qralarm.core.ui.sound.di

import android.app.Service
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SoundModule {

    @Provides
    fun provideMediaPlayer(): MediaPlayer = MediaPlayer()

    @Suppress("DEPRECATION")
    @Provides
    fun provideVibrator(
        @ApplicationContext context: Context
    ): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Service.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        }

    @Provides
    fun provideAlarmRingtonePlayer(
        @ApplicationContext context: Context,
        mediaPlayer: MediaPlayer,
        vibrator: Vibrator
    ): AlarmRingtonePlayer = AlarmRingtonePlayer(context, mediaPlayer, vibrator)
}