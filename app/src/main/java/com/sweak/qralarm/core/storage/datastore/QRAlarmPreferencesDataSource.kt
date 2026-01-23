package com.sweak.qralarm.core.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sweak.qralarm.core.domain.user.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QRAlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setIntroductionFinished(finished: Boolean) {
        dataStore.edit { preferences ->
            preferences[INTRODUCTION_FINISHED] = finished
        }
    }

    fun getIntroductionFinished(): Flow<Boolean?> {
        return dataStore.data.map { preferences ->
            preferences[INTRODUCTION_FINISHED]
        }
    }

    suspend fun setOptimizationGuideState(state: String) {
        dataStore.edit { preferences ->
            preferences[OPTIMIZATION_GUIDE_STATE] = state
        }
    }

    fun getOptimizationGuideState(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[OPTIMIZATION_GUIDE_STATE]
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

    fun getAlarmMissedDetected(): Flow<Boolean?> {
        return dataStore.data.map { preferences ->
            preferences[ALARM_MISSED_DETECTED]
        }
    }

    suspend fun setNextRatePromptTimeInMillis(promptTime: Long) {
        dataStore.edit { preferences ->
            preferences[NEXT_RATE_PROMPT_TIME] = promptTime
        }
    }

    fun getNextRatePromptTimeInMillis(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[NEXT_RATE_PROMPT_TIME]
        }
    }

    suspend fun setDefaultAlarmCode(code: String?) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALARM_CODE] = code ?: ""
        }
    }

    fun getDefaultAlarmCode(): Flow<String?> {
        return dataStore.data.map { preferences ->
            val code = preferences[DEFAULT_ALARM_CODE]

            if (code.isNullOrEmpty()) return@map null
            else return@map code
        }
    }

    suspend fun setEmergencySliderRange(range: IntRange) {
        dataStore.edit { preferences ->
            preferences[EMERGENCY_SLIDER_RANGE] =
                byteArrayOf(range.first.toByte(), range.last.toByte())
        }
    }

    fun getEmergencySliderRange(): Flow<IntRange?> {
        return dataStore.data.map { preferences ->
            val byteArray = preferences[EMERGENCY_SLIDER_RANGE]

            if (byteArray == null || byteArray.size != 2) {
                return@map null
            } else {
                return@map byteArray[0].toInt()..byteArray[1].toInt()
            }
        }
    }

    suspend fun setEmergencyRequiredMatches(matches: Int) {
        dataStore.edit { preferences ->
            preferences[EMERGENCY_REQUIRED_MATCHES] = matches
        }
    }

    fun getEmergencyRequiredMatches(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[EMERGENCY_REQUIRED_MATCHES]
        }
    }

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[THEME] = json.encodeToString(theme)
        }
    }

    fun getTheme(): Flow<Theme?> {
        return dataStore.data.map { preferences ->
            preferences[THEME]?.let {
                try {
                    json.decodeFromString<Theme>(it)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    companion object {
        val TEMPORARY_SCANNED_CODE = stringPreferencesKey("temporaryScannedCode")
        val OPTIMIZATION_GUIDE_STATE = stringPreferencesKey("optimizationGuideState")
        val INTRODUCTION_FINISHED = booleanPreferencesKey("introductionFinished")
        val ALARM_MISSED_DETECTED = booleanPreferencesKey("alarmMissedDetected")
        val NEXT_RATE_PROMPT_TIME = longPreferencesKey("nextRatePromptTime")
        val DEFAULT_ALARM_CODE = stringPreferencesKey("defaultAlarmCode")
        val EMERGENCY_SLIDER_RANGE = byteArrayPreferencesKey("emergencySliderRange")
        val EMERGENCY_REQUIRED_MATCHES = intPreferencesKey("emergencyRequiredMatches")
        val THEME = stringPreferencesKey("theme")
    }
}