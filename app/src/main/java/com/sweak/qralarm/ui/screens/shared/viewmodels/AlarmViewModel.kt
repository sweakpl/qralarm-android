package com.sweak.qralarm.ui.screens.shared.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.QRAlarmService
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.home.HomeUiState
import com.sweak.qralarm.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ExperimentalPermissionsApi
@HiltViewModel
class AlarmViewModel @Inject constructor(
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
                getAlarmMeridiem(alarmTimeInMillis),
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first(),
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SERVICE_RUNNING).first(),
                snoozeAvailable = false,
                showAlarmPermissionDialog = false,
                showCameraPermissionDialog = false,
                showCameraPermissionRevokedDialog = false
            )
        )
    }

    init {
        viewModelScope.launch {
            dataStoreManager.getBoolean(DataStoreManager.ALARM_SERVICE_RUNNING).collect {
                homeUiState.value = homeUiState.value.copy(alarmServiceRunning = it)
            }
        }
        viewModelScope.launch {
            dataStoreManager.getInt(DataStoreManager.SNOOZE_AVAILABLE_COUNT).collect {
                homeUiState.value = homeUiState.value.copy(snoozeAvailable = it > 0)
            }
        }
    }

    fun handleStartOrStopButtonClick(
        navController: NavHostController,
        cameraPermissionState: PermissionState
    ) {
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
                qrAlarmManager.setAlarm(
                    getAlarmTimeInMillis(
                        homeUiState.value.hour,
                        homeUiState.value.minute,
                        homeUiState.value.timeFormat,
                        homeUiState.value.meridiem
                    ),
                    QRAlarmService.ALARM_TYPE_NORMAL
                )

                viewModelScope.launch {
                    dataStoreManager.apply {
                        putBoolean(DataStoreManager.ALARM_SET, true)
                        homeUiState.value = homeUiState.value.copy(alarmSet = true)

                        putLong(
                            DataStoreManager.ALARM_TIME_IN_MILLIS,
                            getAlarmTimeInMillis(
                                homeUiState.value.hour,
                                homeUiState.value.minute,
                                homeUiState.value.timeFormat,
                                homeUiState.value.meridiem
                            )
                        )

                        putInt(
                            DataStoreManager.SNOOZE_AVAILABLE_COUNT,
                            getInt(DataStoreManager.SNOOZE_MAX_COUNT).first()
                        )
                    }
                }
            } catch (exception: SecurityException) {
                homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                return
            }
        } else {
            navController.navigate(Screen.ScannerScreen.route)
        }
    }

    fun stopAlarm() {
        qrAlarmManager.cancelAlarm()

        viewModelScope.launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, false)

                val originalAlarmTimeInMillis =
                    getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()

                homeUiState.value = homeUiState.value.copy(
                    alarmSet = false,
                    hour = getAlarmHour(originalAlarmTimeInMillis, homeUiState.value.timeFormat),
                    minute = getAlarmMinute(originalAlarmTimeInMillis),
                    meridiem = getAlarmMeridiem(originalAlarmTimeInMillis)
                )
            }
        }
    }

    fun handleSnoozeButtonClick() {
        qrAlarmManager.cancelAlarm()

        viewModelScope.launch {
            dataStoreManager.apply {
                val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(
                    getInt(DataStoreManager.SNOOZE_DURATION_MINUTES).first()
                )

                try {
                    qrAlarmManager.setAlarm(
                        snoozeAlarmTimeInMillis,
                        QRAlarmService.ALARM_TYPE_SNOOZE
                    )
                } catch (exception: SecurityException) {
                    homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                    return@launch
                }

                putBoolean(DataStoreManager.ALARM_SET, true)
                homeUiState.value = homeUiState.value.copy(alarmSet = true)

                val alarmHour = getAlarmHour(
                    snoozeAlarmTimeInMillis,
                    homeUiState.value.timeFormat
                )
                val alarmMinute = getAlarmMinute(snoozeAlarmTimeInMillis)
                val alarmMeridiem = getAlarmMeridiem(snoozeAlarmTimeInMillis)

                putLong(
                    DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS,
                    getAlarmTimeInMillis(
                        alarmHour,
                        alarmMinute,
                        homeUiState.value.timeFormat,
                        alarmMeridiem
                    )
                )

                homeUiState.value = homeUiState.value.copy(
                    hour = alarmHour,
                    minute = alarmMinute,
                    meridiem = alarmMeridiem
                )

                val availableSnoozes = getInt(DataStoreManager.SNOOZE_AVAILABLE_COUNT).first()
                putInt(DataStoreManager.SNOOZE_AVAILABLE_COUNT, availableSnoozes - 1)
            }
        }
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getInt(DataStoreManager.ALARM_TIME_FORMAT).first()

        return TimeFormat.fromInt(timeFormat) ?: TimeFormat.MILITARY
    }
}