package com.sweak.qralarm.alarm.di

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideQRAlarmManager(
        alarmManager: AlarmManager,
        notificationManager: NotificationManager,
        alarmsRepository: AlarmsRepository,
        @ApplicationContext context: Context
    ): QRAlarmManager = QRAlarmManager(alarmManager, notificationManager, alarmsRepository, context)

    @Provides
    fun provideAlarmManager(app: Application): AlarmManager =
        app.getSystemService(Service.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideNotificationManager(app: Application): NotificationManager =
        app.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
}