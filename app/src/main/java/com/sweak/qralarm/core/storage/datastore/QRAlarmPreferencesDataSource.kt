package com.sweak.qralarm.core.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QRAlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
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

    private companion object {
        val TEMPORARY_SCANNED_CODE = stringPreferencesKey("temporaryScannedCode")
    }
}