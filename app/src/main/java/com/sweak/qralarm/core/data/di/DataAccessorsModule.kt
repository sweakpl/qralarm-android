package com.sweak.qralarm.core.data.di

import com.sweak.qralarm.core.data.alarm.AlarmRingtoneStorageImpl
import com.sweak.qralarm.core.data.alarm.AlarmsRepositoryImpl
import com.sweak.qralarm.core.data.alarm.CodesRepositoryImpl
import com.sweak.qralarm.core.data.backup.BackupRepositoryImpl
import com.sweak.qralarm.core.data.backup.ZipBackupCodec
import com.sweak.qralarm.core.data.custom_object.CustomObjectsRepositoryImpl
import com.sweak.qralarm.core.data.emergency.EmergencyExplanationsRepositoryImpl
import com.sweak.qralarm.core.data.user.UserDataRepositoryImpl
import com.sweak.qralarm.core.domain.custom_object.CustomObjectsRepository
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanationsRepository
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.backup.BackupCodec
import com.sweak.qralarm.core.domain.backup.BackupRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataAccessorsModule {

    @Binds
    fun bindsUserDataRepository(
        userDataRepository: UserDataRepositoryImpl
    ): UserDataRepository

    @Binds
    fun bindsAlarmsRepository(
        alarmsRepositoryImpl: AlarmsRepositoryImpl
    ): AlarmsRepository

    @Binds
    fun bindsCodesRepository(
        codesRepositoryImpl: CodesRepositoryImpl
    ): CodesRepository

    @Binds
    fun bindsAlarmRingtoneStorage(
        alarmRingtoneStorageImpl: AlarmRingtoneStorageImpl
    ): AlarmRingtoneStorage

    @Binds
    fun bindsBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl
    ): BackupRepository

    @Binds
    fun bindsEmergencyExplanationsRepository(
        impl: EmergencyExplanationsRepositoryImpl
    ): EmergencyExplanationsRepository

    @Binds
    fun bindsCustomObjectsRepository(
        impl: CustomObjectsRepositoryImpl
    ): CustomObjectsRepository

    @Binds
    fun bindsBackupCodec(
        zipBackupCodec: ZipBackupCodec
    ): BackupCodec
}