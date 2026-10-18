package com.sweak.qralarm.di

import com.sweak.qralarm.core.data.time.SystemDefaultZoneClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object GlobalModule {

    @Provides
    fun provideClock(): Clock = SystemDefaultZoneClock
}
