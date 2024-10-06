package com.sweak.qralarm.core.ui.sound.di

import android.content.Context
import android.media.MediaPlayer
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

    @Provides
    fun provideAlarmRingtonePlayer(
        @ApplicationContext context: Context,
        mediaPlayer: MediaPlayer
    ): AlarmRingtonePlayer = AlarmRingtonePlayer(context, mediaPlayer)
}