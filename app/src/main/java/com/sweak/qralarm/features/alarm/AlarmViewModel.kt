package com.sweak.qralarm.features.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.alarm.SnoozeAlarm
import com.sweak.qralarm.core.ui.compose_util.UiText
import com.sweak.qralarm.features.alarm.navigation.ID_OF_ALARM
import com.sweak.qralarm.features.alarm.navigation.IS_TRANSIENT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var isAlarmBeingStopped: Boolean = false
    private val isTransient: Boolean = savedStateHandle.get<Boolean>(IS_TRANSIENT) != false

    private var _state = MutableStateFlow(AlarmScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<AlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarmFlow(alarmId = idOfAlarm).collect {
                alarm = it

                if (!isAlarmBeingStopped) {
                    val isAlarmSnoozed = it.snoozeConfig.isAlarmSnoozed
                    val isSnoozeAvailable =
                        it.snoozeConfig.numberOfSnoozesLeft != 0 && !isAlarmSnoozed
                    val isAlarmRunning = it.isAlarmRunning
                    val isInteractionEnabled = isAlarmRunning || (isAlarmSnoozed && !isTransient)
                    val alarmLabel = if (isAlarmRunning) {
                        if (it.alarmLabel != null) {
                            UiText.DynamicString(it.alarmLabel)
                        } else {
                            UiText.StringResource(resId = R.string.alarm_wake_up)
                        }
                    } else if (isAlarmSnoozed) {
                        UiText.StringResource(resId = R.string.alarm_snoozed_until)
                    } else null
                    val timeToShow =
                        if (isAlarmSnoozed && it.snoozeConfig.nextSnoozedAlarmTimeInMillis != null) {
                            it.snoozeConfig.nextSnoozedAlarmTimeInMillis
                        } else {
                            System.currentTimeMillis()
                        }

                    _state.update { currentState ->
                        currentState.copy(
                            alarmLabel = alarmLabel,
                            timeToShow = timeToShow,
                            isAlarmSnoozed = isAlarmSnoozed,
                            isSnoozeAvailable = isSnoozeAvailable,
                            isInteractionEnabled = isInteractionEnabled,
                            isEmergencyAvailable = it.isUsingCode && it.isEmergencyTaskEnabled
                        )
                    }
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
                        isAlarmBeingStopped = true
                        alarmsRepository.setAlarmSnoozed(
                            alarmId = idOfAlarm,
                            snoozed = false
                        )
                        handleAlarmRescheduling()

                        backendEventsChannel.send(AlarmScreenBackendEvent.StopAlarm)
                    }
                    return
                }

                _state.update { currentState ->
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
                        if (alarm.temporaryMuteDurationInSeconds > 0) {
                            viewModelScope.launch {
                                backendEventsChannel.send(
                                    AlarmScreenBackendEvent.TryTemporarilyMuteAlarm(
                                        muteDurationInSeconds = alarm.temporaryMuteDurationInSeconds
                                    )
                                )
                            }
                        }

                        return@update currentState.copy(
                            permissionsDialogState =
                            AlarmScreenState.PermissionsDialogState(
                                isVisible = true,
                                cameraPermissionState = false
                            )
                        )
                    }

                    viewModelScope.launch {
                        if (alarm.temporaryMuteDurationInSeconds > 0) {
                            backendEventsChannel.send(
                                AlarmScreenBackendEvent.TryTemporarilyMuteAlarm(
                                    muteDurationInSeconds = alarm.temporaryMuteDurationInSeconds
                                )
                            )
                        }

                        backendEventsChannel.send(
                            AlarmScreenBackendEvent.RequestCodeScanToStopAlarm
                        )
                    }

                    return@update currentState
                }
            }
            is AlarmScreenUserEvent.SnoozeAlarmClicked -> {
                viewModelScope.launch {
                    _state.update { currentState ->
                        currentState.copy(isInteractionEnabled = !isTransient)
                    }

                    snoozeAlarm(
                        alarmId = idOfAlarm,
                        isReschedulingCurrentOrMissedSnooze = false
                    )

                    backendEventsChannel.send(AlarmScreenBackendEvent.SnoozeAlarm)
                }
            }
            is AlarmScreenUserEvent.HideMissingPermissionsDialog -> {
                _state.update { currentState ->
                    currentState.copy(
                        permissionsDialogState = AlarmScreenState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is AlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is AlarmScreenUserEvent.UpdateCurrentTime -> {
                if (!_state.value.isAlarmSnoozed) {
                    _state.update { currentState ->
                        currentState.copy(timeToShow = System.currentTimeMillis())
                    }
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
                setAlarm(
                    alarmId = alarm.alarmId,
                    isReschedulingMissedAlarm = false
                )
            }
        }
    }
}