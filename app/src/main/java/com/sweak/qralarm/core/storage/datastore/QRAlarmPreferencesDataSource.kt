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
import kotlinx.coroutines.flow.first
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

    suspend fun getLegacyDefaultAlarmCodeValue(): String? {
        return dataStore.data.map { preferences ->
            val code = preferences[DEFAULT_ALARM_CODE]
            if (code.isNullOrEmpty()) null else code
        }.first()
    }

    suspend fun clearLegacyDefaultAlarmCode() {
        dataStore.edit { preferences ->
            preferences.remove(DEFAULT_ALARM_CODE)
        }
    }

    suspend fun setDefaultAlarmCodeId(codeId: Long?) {
        dataStore.edit { preferences ->
            if (codeId == null) {
                preferences.remove(DEFAULT_ALARM_CODE_ID)
            } else {
                preferences[DEFAULT_ALARM_CODE_ID] = codeId
            }
        }
    }

    fun getDefaultAlarmCodeId(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[DEFAULT_ALARM_CODE_ID]
        }
    }

    suspend fun setHasMigratedDefaultAlarmCode(migrated: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_MIGRATED_DEFAULT_ALARM_CODE] = migrated
        }
    }

    fun getHasMigratedDefaultAlarmCode(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[HAS_MIGRATED_DEFAULT_ALARM_CODE] == true
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

    suspend fun setWhatsNewLastShownVersionCode(versionCode: Int) {
        dataStore.edit { preferences ->
            preferences[WHATS_NEW_LAST_SHOWN_VERSION_CODE] = versionCode
        }
    }

    fun getWhatsNewLastShownVersionCode(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[WHATS_NEW_LAST_SHOWN_VERSION_CODE]
        }
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
        val DEFAULT_ALARM_CODE_ID = longPreferencesKey("defaultAlarmCodeId")
        val HAS_MIGRATED_DEFAULT_ALARM_CODE = booleanPreferencesKey("hasMigratedDefaultAlarmCode")
        val EMERGENCY_SLIDER_RANGE = byteArrayPreferencesKey("emergencySliderRange")
        val EMERGENCY_REQUIRED_MATCHES = intPreferencesKey("emergencyRequiredMatches")
        val THEME = stringPreferencesKey("theme")
        val WHATS_NEW_LAST_SHOWN_VERSION_CODE = intPreferencesKey("whatsNewLastShownVersionCode")
    }
}