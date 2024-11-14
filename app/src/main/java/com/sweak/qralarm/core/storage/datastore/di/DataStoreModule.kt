package com.sweak.qralarm.core.storage.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.preferencesDataStoreFile
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource.Companion.INTRODUCTION_FINISHED
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource.Companion.LEGACY_DATA_MIGRATED
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("QRAlarmPreferences") },
            corruptionHandler = ReplaceFileCorruptionHandler {
                preferencesOf(
                    INTRODUCTION_FINISHED to true,
                    LEGACY_DATA_MIGRATED to true
                )
            }
        )
}