package com.sweak.qralarm.ui.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
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

@ExperimentalPermissionsApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager
) : ViewModel() {

    val homeUiState: MutableState<HomeUiState> = runBlocking {
        val alarmTimeInMillis =
            dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
        val timeFormat = getTimeFormat()

        mutableStateOf(
            HomeUiState(
                timeFormat,
                getAlarmHour(alarmTimeInMillis, timeFormat),
                getAlarmMinute(alarmTimeInMillis),
                getMeridiem(alarmTimeInMillis),
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first(),
                showAlarmPermissionDialog = false,
                showCameraPermissionDialog = false,
                showCameraPermissionRevokedDialog = false
            )
        )
    }

    fun startOrStopAlarm(cameraPermissionState: PermissionState) {
        if (!cameraPermissionState.hasPermission) {
            when {
                !cameraPermissionState.permissionRequested ||
                        cameraPermissionState.shouldShowRationale -> {
                    homeUiState.value = homeUiState.value.copy(showCameraPermissionDialog = true)
                    return
                }
                !cameraPermissionState.shouldShowRationale -> {
                    homeUiState.value =
                        homeUiState.value.copy(showCameraPermissionRevokedDialog = true)
                    return
                }
            }
        }

        val alarmSet = homeUiState.value.alarmSet

        if (!alarmSet) {
            try {
                qrAlarmManager.setAlarm(getAlarmTimeInMillis(), QRAlarmService.ALARM_TYPE_NORMAL)
            } catch (exception: SecurityException) {
                homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                return
            }
        } else {
            qrAlarmManager.cancelAlarm()
        }

        viewModelScope.launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, !alarmSet)
                homeUiState.value = homeUiState.value.copy(alarmSet = !alarmSet)

                putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, getAlarmTimeInMillis())
            }
        }
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getString(DataStoreManager.ALARM_TIME_FORMAT).first()

        return if (timeFormat == TimeFormat.AMPM.name) TimeFormat.AMPM else TimeFormat.MILITARY
    }

    private fun getAlarmHour(alarmTimeInMillis: Long, timeFormat: TimeFormat): Int {
        val alarmCalendar = Calendar.getInstance().apply {
            timeInMillis = alarmTimeInMillis
        }

        return if (timeFormat == TimeFormat.MILITARY) {
            alarmCalendar.get(Calendar.HOUR_OF_DAY)
        } else {
            alarmCalendar.get(Calendar.HOUR).apply {
                return if (this == 0) 12 else this
            }
        }
    }

    private fun getAlarmMinute(alarmTimeInMillis: Long): Int {
        Calendar.getInstance().apply {
            timeInMillis = alarmTimeInMillis
            return get(Calendar.MINUTE)
        }
    }

    private fun getMeridiem(alarmTimeInMillis: Long): Meridiem {
        Calendar.getInstance().apply {
            timeInMillis = alarmTimeInMillis
            return if (get(Calendar.AM_PM) == Calendar.AM) Meridiem.AM else Meridiem.PM
        }
    }

    private fun getAlarmTimeInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            with(homeUiState.value) {
                set(
                    if (timeFormat == TimeFormat.MILITARY) Calendar.HOUR_OF_DAY else Calendar.HOUR,
                    when {
                        timeFormat == TimeFormat.MILITARY -> hour
                        hour == 12 -> 0
                        else -> hour
                    }
                )
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeFormat == TimeFormat.AMPM) {
                    set(Calendar.AM_PM, if (meridiem == Meridiem.AM) Calendar.AM else Calendar.PM)
                }
            }
        }

        if (calendar.timeInMillis <= Calendar.getInstance().timeInMillis) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }

        return calendar.timeInMillis
    }
}