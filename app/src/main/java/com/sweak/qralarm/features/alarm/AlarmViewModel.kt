package com.sweak.qralarm.features.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.alarm.SnoozeAlarm
import com.sweak.qralarm.features.alarm.navigation.ID_OF_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository,
    private val snoozeAlarm: SnoozeAlarm,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private lateinit var alarm: Alarm

    var state = MutableStateFlow(AlarmScreenState())

    private val backendEventsChannel = Channel<AlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarmFlow(alarmId = idOfAlarm).collect {
                alarm = it

                state.update { currentState ->
                    currentState.copy(
                        isAlarmSnoozed = it.snoozeConfig.isAlarmSnoozed,
                        snoozedAlarmTimeInMillis = it.snoozeConfig.nextSnoozedAlarmTimeInMillis,
                        isSnoozeAvailable = it.snoozeConfig.numberOfSnoozesLeft != 0
                    )
                }
            }
        }
    }

    fun onEvent(event: AlarmScreenUserEvent) {
        when (event) {
            is AlarmScreenUserEvent.TryStopAlarm -> {
                val isUsingCode = ::alarm.isInitialized && alarm.isUsingCode

                if (!isUsingCode) {
                    viewModelScope.launch {
                        alarmsRepository.setAlarmSnoozed(
                            alarmId = idOfAlarm,
                            snoozed = false
                        )
                        handleAlarmRescheduling()

                        backendEventsChannel.send(AlarmScreenBackendEvent.StopAlarm)
                    }
                    return
                }

                state.update { currentState ->
                    if (currentState.permissionsDialogState.isVisible) {
                        if (currentState.permissionsDialogState.cameraPermissionState == true) {
                            viewModelScope.launch {
                                backendEventsChannel.send(
                                    AlarmScreenBackendEvent.RequestCodeScanToStopAlarm
                                )
                            }

                            return@update currentState.copy(
                                permissionsDialogState =
                                AlarmScreenState.PermissionsDialogState(
                                    isVisible = false
                                )
                            )
                        } else {
                            return@update currentState.copy(
                                permissionsDialogState = currentState.permissionsDialogState.copy(
                                    cameraPermissionState =
                                    currentState.permissionsDialogState.cameraPermissionState?.let {
                                        event.cameraPermissionStatus
                                    }
                                )
                            )
                        }
                    }

                    if (!event.cameraPermissionStatus) {
                        return@update currentState.copy(
                            permissionsDialogState =
                            AlarmScreenState.PermissionsDialogState(
                                isVisible = true,
                                cameraPermissionState = false
                            )
                        )
                    }

                    viewModelScope.launch {
                        backendEventsChannel.send(
                            AlarmScreenBackendEvent.RequestCodeScanToStopAlarm
                        )
                    }

                    return@update currentState
                }
            }
            is AlarmScreenUserEvent.SnoozeAlarmClicked -> {
                viewModelScope.launch {
                    snoozeAlarm(alarmId = idOfAlarm)
                    backendEventsChannel.send(AlarmScreenBackendEvent.SnoozeAlarm)
                }
            }
            is AlarmScreenUserEvent.HideMissingPermissionsDialog -> {
                state.update { currentState ->
                    currentState.copy(
                        permissionsDialogState = AlarmScreenState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is AlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            else -> { /* no-op */ }
        }
    }

    private suspend fun handleAlarmRescheduling() {
        if (::alarm.isInitialized) {
            if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                disableAlarm(alarmId = alarm.alarmId)
            } else if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                setAlarm(alarmId = alarm.alarmId)
            }
        }
    }
}