package com.sweak.qralarm.core.data.di

import com.sweak.qralarm.core.data.alarm.AlarmRingtoneStorageImpl
import com.sweak.qralarm.core.data.alarm.AlarmsRepositoryImpl
import com.sweak.qralarm.core.data.alarm.CodesRepositoryImpl
import com.sweak.qralarm.core.data.user.UserDataRepositoryImpl
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
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
}