package com.sweak.qralarm.alarm.di

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.Service
import com.sweak.qralarm.alarm.QRAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideQRAlarmManager(
        alarmManager: AlarmManager,
        notificationManager: NotificationManager
    ): QRAlarmManager = QRAlarmManager(alarmManager, notificationManager)

    @Provides
    fun provideAlarmManager(app: Application): AlarmManager =
        app.getSystemService(Service.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideNotificationManager(app: Application): NotificationManager =
        app.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
}