package com.sweak.qralarm.features.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.features.alarm.navigation.ID_OF_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class AlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private var isUsingCode by Delegates.notNull<Boolean>()

    var state = MutableStateFlow(AlarmScreenState())

    private val backendEventsChannel = Channel<AlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarm(alarmId = idOfAlarm)?.let {
                isUsingCode = it.isUsingCode
            }
        }
    }

    fun onEvent(event: AlarmScreenUserEvent) {
        when (event) {
            is AlarmScreenUserEvent.TryStopAlarm -> {
                if (!isUsingCode) {
                    viewModelScope.launch {
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
}