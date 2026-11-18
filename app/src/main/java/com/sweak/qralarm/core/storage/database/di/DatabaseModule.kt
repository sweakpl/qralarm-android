package com.sweak.qralarm.core.storage.database.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import com.sweak.qralarm.core.storage.database.QRAlarmDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.dao.CodesDao
import com.sweak.qralarm.core.storage.database.dao.CustomObjectsDao
import com.sweak.qralarm.core.storage.database.dao.EmergencyExplanationsDao
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
            context.createDeviceProtectedStorageContext(),
            QRAlarmDatabase::class.java,
            "QRAlarmDatabase"
        )
            .addMigrations(
                QRAlarmDatabase.MIGRATION_3_4,
                QRAlarmDatabase.MIGRATION_7_8,
                QRAlarmDatabase.MIGRATION_8_9,
                QRAlarmDatabase.MIGRATION_9_10,
                QRAlarmDatabase.MIGRATION_10_11,
                QRAlarmDatabase.MIGRATION_11_12
            )
            .build()

    @Provides
    fun provideAlarmsDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): AlarmsDao = qrAlarmDatabase.alarmsDao()

    @Provides
    fun provideCustomObjectsDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): CustomObjectsDao = qrAlarmDatabase.customObjectsDao()

    @Provides
    fun provideEmergencyExplanationsDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): EmergencyExplanationsDao = qrAlarmDatabase.emergencyExplanationsDao()

    @Provides
    fun provideCodesDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): CodesDao = qrAlarmDatabase.codesDao()
}
