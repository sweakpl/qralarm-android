package com.sweak.qralarm.core.storage.database.di

import android.content.Context
import androidx.room.Room
import com.sweak.qralarm.core.storage.database.QRAlarmDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQRAlarmDatabase(
        @ApplicationContext context: Context
    ): QRAlarmDatabase =
        Room.databaseBuilder(
            context,
            QRAlarmDatabase::class.java,
            "QRAlarmDatabase"
        ).build()

    @Provides
    fun provideAlarmsDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): AlarmsDao = qrAlarmDatabase.alarmsDao()
}