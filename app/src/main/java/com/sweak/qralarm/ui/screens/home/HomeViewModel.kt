package com.sweak.qralarm.ui.screens.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.navigateThrottled
import com.sweak.qralarm.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager
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
                alarmTimeFormat = timeFormat,
                alarmHourOfDay = getAlarmHourOfDay(alarmTimeInMillis),
                alarmMinute = getAlarmMinute(alarmTimeInMillis),
                alarmSet = dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first(),
                alarmServiceRunning =
                dataStoreManager.getBoolean(DataStoreManager.ALARM_SERVICE_RUNNING).first(),
                snoozeAvailable = false,
                isManualAlarmScheduling =
                dataStoreManager.getBoolean(DataStoreManager.MANUAL_ALARM_SCHEDULING).first(),
                showAlarmPermissionDialog = false,
                showCameraPermissionDialog = false,
                showCameraPermissionRevokedDialog = false,
                showNotificationsPermissionDialog = false,
                showNotificationsPermissionRevokedDialog = false,
                showCodePossessionConfirmationDialog = false,
                showFullScreenIntentPermissionDialog = false,
                showEnableRepeatingAlarmsDialog = false,
                showDisableRepeatingAlarmsDialog = false,
                snackbarHostState = SnackbarHostState()
            )
        )
    }

    // This flag is used to prevent the changing of user-set alarm time to the one stored in
    // DataStoreManager due to updating the SHOULD_REMIND_USER_TO_GET_CODE field or any fields when
    // on the SettingsScreen which causes an additional and redundant broadcast of the ALARM_SET
    // value in a FlowCollector. This is an issue internal to the DataStoreManager and not the
    // DataStore framework itself.
    var shouldNotUpdateAlarmStateDataStoreManagerUpdate: Boolean = false

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
        viewModelScope.launch {
            dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).collect {
                if (shouldNotUpdateAlarmStateDataStoreManagerUpdate) {
                    shouldNotUpdateAlarmStateDataStoreManagerUpdate = false
                    return@collect
                }

                homeUiState.value = if (!it) {
                    val originalAlarmTimeInMillis =
                        dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()

                    homeUiState.value.copy(
                        alarmSet = false,
                        alarmHourOfDay = getAlarmHourOfDay(originalAlarmTimeInMillis),
                        alarmMinute = getAlarmMinute(originalAlarmTimeInMillis)
                    )
                } else {
                    homeUiState.value.copy(alarmSet = true)
                }

                if (!it) homeUiState.value.snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun handleStartOrStopButtonClick(
        navController: NavHostController,
        cameraPermissionState: PermissionState,
        notificationsPermissionState: PermissionState,
        composableScope: CoroutineScope,
        lifecycleOwner: LifecycleOwner,
        snackbarInitializer: suspend (Pair<Int, Int>) -> SnackbarResult,
        alarmSoundMuteHandler: () -> Unit
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

        if (!qrAlarmManager.canUseFullScreenIntent()) {
            homeUiState.value = homeUiState.value.copy(
                showFullScreenIntentPermissionDialog = true
            )
            return
        }

        if (!qrAlarmManager.canScheduleExactAlarms()) {
            homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
            return
        }

        val alarmSet = homeUiState.value.alarmSet
        val alarmServiceRunning = homeUiState.value.alarmServiceRunning

        if (!alarmSet && !alarmServiceRunning) {
            viewModelScope.launch {
                val shouldRemindUserToGetCode = dataStoreManager
                    .getBoolean(DataStoreManager.SHOULD_REMIND_USER_TO_GET_CODE)
                    .first()

                if (shouldRemindUserToGetCode) {
                    homeUiState.value = homeUiState.value.copy(
                        showCodePossessionConfirmationDialog = true
                    )
                    return@launch
                }

                val alarmTimeInMillis = getAlarmTimeInMillis(
                    homeUiState.value.alarmHourOfDay,
                    homeUiState.value.alarmMinute
                )
                val alarmTimeZoneId = ZoneId.systemDefault().id

                qrAlarmManager.setAlarm(alarmTimeInMillis, ALARM_TYPE_NORMAL)

                dataStoreManager.apply {
                    putBoolean(DataStoreManager.ALARM_SET, true)
                    putBoolean(DataStoreManager.ALARM_SNOOZED, false)

                    putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, alarmTimeInMillis)
                    putString(DataStoreManager.ALARM_TIME_ZONE_ID, alarmTimeZoneId)

                    putInt(
                        DataStoreManager.SNOOZE_AVAILABLE_COUNT,
                        getInt(DataStoreManager.SNOOZE_MAX_COUNT).first()
                    )
                }

                composableScope.launch {
                    val snackbarResult = snackbarInitializer(
                        getHoursAndMinutesUntilTimePair(alarmTimeInMillis)
                    )

                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> {
                            delay(500)
                            stopAlarm()
                        }
                        SnackbarResult.Dismissed -> { /* no-op */ }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val alarmTimeInMillis =
                    dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
                val currentTimeInMillis = System.currentTimeMillis()

                // If stop request was at least an hour before the alarm - stop immediately...
                if (alarmTimeInMillis - currentTimeInMillis > 3600000) {
                    stopAlarm()
                } else { // ... else start ScannerScreen to disable alarm by scanning the code.
                    // TODO: if (user enabled alarm muting)
                    alarmSoundMuteHandler()

                    navController.navigateThrottled(
                        Screen.ScannerScreen.withArguments(SCAN_MODE_DISMISS_ALARM),
                        lifecycleOwner
                    )
                }
            }
        }
    }

    private fun stopAlarm() = viewModelScope.launch {
        qrAlarmManager.cancelAlarm()

        dataStoreManager.apply {
            putBoolean(DataStoreManager.ALARM_SET, false)
            putBoolean(DataStoreManager.ALARM_SNOOZED, false)

            val originalAlarmTimeInMillis = getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()

            homeUiState.value = homeUiState.value.copy(
                alarmHourOfDay = getAlarmHourOfDay(originalAlarmTimeInMillis),
                alarmMinute = getAlarmMinute(originalAlarmTimeInMillis)
            )
        }
    }

    fun handleSnoozeButtonClick(snoozeSideEffect: () -> Unit) {
        qrAlarmManager.cancelAlarm()

        viewModelScope.launch {
            dataStoreManager.apply {
                val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(
                    getInt(DataStoreManager.SNOOZE_DURATION_MINUTES).first()
                )

                if (!qrAlarmManager.canScheduleExactAlarms()) {
                    homeUiState.value = homeUiState.value.copy(showAlarmPermissionDialog = true)
                    return@launch
                }

                qrAlarmManager.setAlarm(snoozeAlarmTimeInMillis, ALARM_TYPE_SNOOZE)

                putBoolean(DataStoreManager.ALARM_SET, true)
                putBoolean(DataStoreManager.ALARM_SNOOZED, true)

                val alarmHour = getAlarmHourOfDay(snoozeAlarmTimeInMillis)
                val alarmMinute = getAlarmMinute(snoozeAlarmTimeInMillis)
                val alarmTimeZoneId = ZoneId.systemDefault().id

                putLong(
                    DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS,
                    getAlarmTimeInMillis(alarmHour, alarmMinute)
                )
                putString(DataStoreManager.ALARM_TIME_ZONE_ID, alarmTimeZoneId)

                homeUiState.value = homeUiState.value.copy(
                    alarmHourOfDay = alarmHour,
                    alarmMinute = alarmMinute
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

    fun confirmCodePossession() = viewModelScope.launch {
        shouldNotUpdateAlarmStateDataStoreManagerUpdate = true
        dataStoreManager.putBoolean(DataStoreManager.SHOULD_REMIND_USER_TO_GET_CODE, false)
    }

    fun handleRepeatAlarmClick() = viewModelScope.launch {
        val isManualAlarmScheduling = dataStoreManager.getBoolean(
            DataStoreManager.MANUAL_ALARM_SCHEDULING
        ).first()

        if (isManualAlarmScheduling) {
            homeUiState.value = homeUiState.value.copy(
                showEnableRepeatingAlarmsDialog = true
            )
        } else {
            homeUiState.value = homeUiState.value.copy(
                showDisableRepeatingAlarmsDialog = true
            )
        }
    }

    fun handleEnableDisableRepeatingAlarms(repeatingAlarmsEnabled: Boolean) =
        viewModelScope.launch {
            dataStoreManager.putBoolean(
                DataStoreManager.MANUAL_ALARM_SCHEDULING,
                !repeatingAlarmsEnabled
            )
            homeUiState.value =
                homeUiState.value.copy(isManualAlarmScheduling = !repeatingAlarmsEnabled)
        }
}