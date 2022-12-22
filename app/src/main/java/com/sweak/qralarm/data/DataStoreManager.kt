package com.sweak.qralarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

    suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun putLong(key: Preferences.Key<Long>, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun putInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBoolean(key: Preferences.Key<Boolean>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: true
        }

    fun getLong(key: Preferences.Key<Long>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }

    fun getInt(key: Preferences.Key<Int>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }

    fun getString(key: Preferences.Key<String>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: ""
        }

    companion object {
        val FIRST_LAUNCH = booleanPreferencesKey("firstLaunch")

        val ALARM_SET = booleanPreferencesKey("alarmSet")
        val ALARM_SNOOZED = booleanPreferencesKey("alarmSnoozed")
        val ALARM_SERVICE_RUNNING = booleanPreferencesKey("alarmServiceRunning")
        val ALARM_SERVICE_PROPERLY_CLOSED = booleanPreferencesKey("alarmServiceProperlyClosed")
        val DISMISS_ALARM_CODE = stringPreferencesKey("dismissAlarmCode")

        val ALARM_TIME_FORMAT = intPreferencesKey("alarmTimeFormat")
        val ALARM_TIME_IN_MILLIS = longPreferencesKey("alarmTimeInMillis")
        val SNOOZE_ALARM_TIME_IN_MILLIS = longPreferencesKey("snoozeAlarmTimeInMillis")
        
        val ALARM_SOUND = intPreferencesKey("alarmSound")
        val LOCAL_ALARM_SOUND_URI = stringPreferencesKey("localAlarmSoundUri")
        val SNOOZE_MAX_COUNT = intPreferencesKey("snoozeMaxCount")
        val SNOOZE_AVAILABLE_COUNT = intPreferencesKey("snoozeAvailableCount")
        val SNOOZE_DURATION_MINUTES = intPreferencesKey("snoozeDurationMinutes")
        val GENTLE_WAKEUP_DURATION_SECONDS = intPreferencesKey("gentleWakeupDurationSeconds")

        val ALARM_ALARMING = booleanPreferencesKey("alarmIsExecuting")
        val REQUIRE_SCAN_ALWAYS = booleanPreferencesKey("requireScanAlways")
        val ACCEPT_ANY_BARCODE = booleanPreferencesKey("useQRCodesOnly")
        val FAST_MINUTES_CONTROL = booleanPreferencesKey("fastMinutesControl")
    }
}