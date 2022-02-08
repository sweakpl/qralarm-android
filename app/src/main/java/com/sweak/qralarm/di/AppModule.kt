package com.sweak.qralarm.di

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(app: Application): DataStoreManager =
        DataStoreManager(app)

    @Provides
    @Singleton
    fun provideQrAlarmManager(alarmManager: AlarmManager, app: Application): QRAlarmManager =
        QRAlarmManager(alarmManager, app)

    @Provides
    @Singleton
    fun provideAlarmManager(app: Application): AlarmManager =
        app.getSystemService(Service.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideNotificationManager(app: Application): NotificationManager =
        app.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideVibrator(app: Application): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (app.getSystemService(Service.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            app.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        }
}