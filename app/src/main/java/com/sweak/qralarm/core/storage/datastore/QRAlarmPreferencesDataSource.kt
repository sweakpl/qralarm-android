package com.sweak.qralarm.core.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QRAlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setIntroductionFinished(finished: Boolean) {
        dataStore.edit { preferences ->
            preferences[INTRODUCTION_FINISHED] = finished
        }
    }

    fun getIntroductionFinished(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[INTRODUCTION_FINISHED] ?: false
        }
    }

    suspend fun setOptimizationGuideState(state: String) {
        dataStore.edit { preferences ->
            preferences[OPTIMIZATION_GUIDE_STATE] = state
        }
    }

    fun getOptimizationGuideState(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[OPTIMIZATION_GUIDE_STATE] ?: "NONE"
        }
    }

    suspend fun setTemporaryScannedCode(code: String?) {
        dataStore.edit { preferences ->
            preferences[TEMPORARY_SCANNED_CODE] = code ?: ""
        }
    }

    fun getTemporaryScannedCode(): Flow<String?> {
        return dataStore.data.map { preferences ->
            val code = preferences[TEMPORARY_SCANNED_CODE]

            if (code.isNullOrEmpty()) return@map null
            else return@map code
        }
    }

    suspend fun setAlarmMissedDetected(detected: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALARM_MISSED_DETECTED] = detected
        }
    }

    fun getAlarmMissedDetected(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[ALARM_MISSED_DETECTED] ?: false
        }
    }

    suspend fun setNextRatePromptTimeInMillis(promptTime: Long?) {
        dataStore.edit { preferences ->
            preferences[NEXT_RATE_PROMPT_TIME] = promptTime ?: 0L
        }
    }

    fun getNextRatePromptTimeInMillis(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[NEXT_RATE_PROMPT_TIME]
        }
    }

    suspend fun setLegacyDataMigrated(migrated: Boolean) {
        dataStore.edit { preferences ->
            preferences[LEGACY_DATA_MIGRATED] = migrated
        }
    }

    fun getLegacyDataMigrated(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[LEGACY_DATA_MIGRATED] ?: false
        }
    }

    companion object {
        val TEMPORARY_SCANNED_CODE = stringPreferencesKey("temporaryScannedCode")
        val OPTIMIZATION_GUIDE_STATE = stringPreferencesKey("optimizationGuideState")
        val INTRODUCTION_FINISHED = booleanPreferencesKey("introductionFinished")
        val ALARM_MISSED_DETECTED = booleanPreferencesKey("alarmMissedDetected")
        val NEXT_RATE_PROMPT_TIME = longPreferencesKey("nextRatePromptTime")
        val LEGACY_DATA_MIGRATED = booleanPreferencesKey("legacyDataMigrated")
    }
}