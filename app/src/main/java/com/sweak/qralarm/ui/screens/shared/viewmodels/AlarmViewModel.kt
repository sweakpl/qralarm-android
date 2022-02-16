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
                showAlarmPermissionDialog = false,
                showCameraPermissionDialog = false,
                showCameraPermissionRevokedDialog = false
            )
        )
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
                homeUiState.value = homeUiState.value.copy(alarmSet = false)
            }
        }
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getString(DataStoreManager.ALARM_TIME_FORMAT).first()

        return if (timeFormat == TimeFormat.AMPM.name) TimeFormat.AMPM else TimeFormat.MILITARY
    }
}