package com.sweak.qralarm.ui.screens.shared.viewmodels

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.*
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.home.HomeUiState
import com.sweak.qralarm.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@InternalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalPermissionsApi
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val homeUiState: MutableState<HomeUiState> = runBlocking {
        val alarmTimeInMillis =
            if (dataStoreManager.getBoolean(DataStoreManager.ALARM_SNOOZED).first()) {
                dataStoreManager.getLong(DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS).first()
            } else {
                dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
            }
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
                showCameraPermissionRevokedDialog = false,
                showNotificationsPermissionDialog = false,
                showNotificationsPermissionRevokedDialog = false,
                snackbarHostState = SnackbarHostState()
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
        cameraPermissionState: PermissionState,
        notificationsPermissionState: PermissionState,
        composableScope: CoroutineScope
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

        if (!notificationsPermissionState.hasPermission) {
            when {
                !notificationsPermissionState.permissionRequested ||
                        notificationsPermissionState.shouldShowRationale -> {
                    homeUiState.value = homeUiState.value.copy(
                        showNotificationsPermissionDialog = true
                    )
                    return
                }
                !notificationsPermissionState.shouldShowRationale -> {
                    homeUiState.value =
                        homeUiState.value.copy(showNotificationsPermissionRevokedDialog = true)
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
                    ALARM_TYPE_NORMAL
                )

                viewModelScope.launch {
                    dataStoreManager.apply {
                        putBoolean(DataStoreManager.ALARM_SET, true)
                        putBoolean(DataStoreManager.ALARM_SNOOZED, false)
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

                composableScope.launch {
                    val snackbarResult = homeUiState.value.snackbarHostState.showSnackbar(
                        message = resourceProvider.getString(R.string.alarm_set),
                        actionLabel = resourceProvider.getString(R.string.cancel),
                        duration = SnackbarDuration.Long
                    )
                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> {
                            delay(500)
                            stopAlarm()
                        }
                        SnackbarResult.Dismissed -> {}
                    }
                }
            } catch (exception: SecurityException) {
                homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                return
            }
        } else {
            var alarming = false
            var easyDismiss = false
            runBlocking {
                alarming = dataStoreManager.getBoolean(DataStoreManager.ALARM_ALARMING).first()
                easyDismiss = dataStoreManager.getBoolean(DataStoreManager.EASY_DISMISS_BEFORE_ALARM).first()
            }

            if (easyDismiss && !alarming) {
                stopAlarm()
                return
            }

            navController.navigate(Screen.ScannerScreen.withArguments(SCAN_MODE_DISMISS_ALARM))
        }
    }

    fun stopAlarm(): Job {
        qrAlarmManager.cancelAlarm()

        val stopAlarmJob = viewModelScope.launch {
            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, false)
                putBoolean(DataStoreManager.ALARM_SNOOZED, false)
                putBoolean(DataStoreManager.ALARM_ALARMING, false)

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

        return stopAlarmJob
    }

    fun handleSnoozeButtonClick(
        snoozeSideEffect: () -> Unit
    ) {
        qrAlarmManager.cancelAlarm()

        viewModelScope.launch {
            dataStoreManager.apply {
                val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(
                    getInt(DataStoreManager.SNOOZE_DURATION_MINUTES).first()
                )

                try {
                    qrAlarmManager.setAlarm(
                        snoozeAlarmTimeInMillis,
                        ALARM_TYPE_SNOOZE
                    )
                } catch (exception: SecurityException) {
                    homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                    return@launch
                }

                putBoolean(DataStoreManager.ALARM_SET, true)
                putBoolean(DataStoreManager.ALARM_SNOOZED, true)
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

            snoozeSideEffect.invoke()
        }
    }

    private suspend fun getTimeFormat(): TimeFormat {
        val timeFormat = dataStoreManager.getInt(DataStoreManager.ALARM_TIME_FORMAT).first()

        return TimeFormat.fromInt(timeFormat) ?: TimeFormat.MILITARY
    }

    fun getDismissCode(): String {
        return runBlocking {
            dataStoreManager.getString(DataStoreManager.DISMISS_ALARM_CODE).first()
        }
    }
}