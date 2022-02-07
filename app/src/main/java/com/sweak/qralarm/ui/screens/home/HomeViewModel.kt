package com.sweak.qralarm.ui.screens.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.QRAlarmService
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.TimeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager
) : ViewModel() {

    val homeUiState: MutableState<HomeUiState> = runBlocking {
        mutableStateOf(
            HomeUiState(
                getTimeFormat(),
                dataStoreManager.getInt(DataStoreManager.ALARM_HOUR).first(),
                dataStoreManager.getInt(DataStoreManager.ALARM_MINUTE).first(),
                getMeridiem(),
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first()
            )
        )
    }

    fun startOrStopAlarm() {
        val alarmSet = homeUiState.value.alarmSet

        if (!alarmSet) {
            try {
                qrAlarmManager.setAlarm(getAlarmTimeInMillis(), QRAlarmService.ALARM_TYPE_NORMAL)
            } catch (exception: SecurityException) {
                Log.i(
                    "HomeViewModel",
                    "App not allowed to schedule exact alarms - handle it!"
                )
                throw exception
            }
        } else {
            qrAlarmManager.cancelAlarm()
        }

        viewModelScope.launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, !alarmSet)
                homeUiState.value = homeUiState.value.copy(alarmSet = !alarmSet)

                putInt(DataStoreManager.ALARM_HOUR, homeUiState.value.hour)
                putInt(DataStoreManager.ALARM_MINUTE, homeUiState.value.minute)

                if (getString(DataStoreManager.ALARM_TIME_FORMAT).first() == TimeFormat.AMPM.name) {
                    putString(DataStoreManager.ALARM_MERIDIEM, homeUiState.value.meridiem.name)
                }
            }
        }
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getString(DataStoreManager.ALARM_TIME_FORMAT).first()

        return if (timeFormat == TimeFormat.AMPM.name) TimeFormat.AMPM else TimeFormat.MILITARY
    }

    private suspend fun getMeridiem(): Meridiem {
        val meridiem = dataStoreManager.getString(DataStoreManager.ALARM_MERIDIEM).first()

        return if (meridiem == Meridiem.PM.name) Meridiem.PM else Meridiem.AM
    }

    private fun getAlarmTimeInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            with(homeUiState.value) {
                set(
                    if (timeFormat == TimeFormat.MILITARY) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                    hour
                )
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        if (calendar.timeInMillis <= Calendar.getInstance().timeInMillis) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }

        return calendar.timeInMillis
    }
}