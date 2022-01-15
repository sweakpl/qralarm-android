package com.sweak.qralarm.ui.screens.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.TimeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val homeUiState: MutableState<HomeUiState> = runBlocking {
        mutableStateOf(
            HomeUiState(
                getTimeFormat(),
                dataStoreManager.getInt(DataStoreManager.ALARM_HOUR).first(),
                dataStoreManager.getInt(DataStoreManager.ALARM_MINUTE).first(),
                getMeridiem()
            )
        )
    }

    fun startAlarm() {
        Log.i(
            "HomeViewModel",
            "Selected time is: " +
                    "${homeUiState.value.hour}:${homeUiState.value.minute}" +
                    if (homeUiState.value.timeFormat == TimeFormat.AMPM) {
                        when (homeUiState.value.meridiem.ordinal) {
                            Meridiem.AM.ordinal -> " AM"
                            else -> " PM"
                        }
                    } else ""
        )
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getString(DataStoreManager.ALARM_TIME_FORMAT).first()

        return if (timeFormat == TimeFormat.AMPM.name) TimeFormat.AMPM else TimeFormat.MILITARY
    }

    private suspend fun getMeridiem(): Meridiem {
        val meridiem = dataStoreManager.getString(DataStoreManager.ALARM_MERIDIEM).first()

        return if (meridiem == Meridiem.PM.name) Meridiem.PM else Meridiem.AM
    }
}