package com.sweak.qralarm.core.storage.database.di

import android.content.Context
import android.os.Build
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createDeviceProtectedStorageContext()
            } else context,
            QRAlarmDatabase::class.java,
            "QRAlarmDatabase"
        )
            .addMigrations(QRAlarmDatabase.MIGRATION_3_4)
            .build()

    @Provides
    fun provideAlarmsDao(
        qrAlarmDatabase: QRAlarmDatabase
    ): AlarmsDao = qrAlarmDatabase.alarmsDao()
}