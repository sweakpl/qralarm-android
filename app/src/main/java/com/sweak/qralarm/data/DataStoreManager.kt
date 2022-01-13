package com.sweak.qralarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun putInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getString(key: Preferences.Key<String>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: "null"
        }

    fun getInt(key: Preferences.Key<Int>) =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }

    companion object {
        val ALARM_HOUR = intPreferencesKey("alarmHour")
        val ALARM_MINUTE = intPreferencesKey("alarmMinute")
        val ALARM_TIME_FORMAT = stringPreferencesKey("alarmTimeFormat")
        val ALARM_MERIDIEM = stringPreferencesKey("alarmMeridiem")
    }
}