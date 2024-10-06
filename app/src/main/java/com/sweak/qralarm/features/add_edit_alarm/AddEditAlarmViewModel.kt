package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmRingtone
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    private val alarmRingtonePlayer: AlarmRingtonePlayer
): ViewModel() {

    var state = MutableStateFlow(AddEditAlarmScreenState())

    private val backendEventsChannel = Channel<AddEditAlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        val dateTime = ZonedDateTime.now()

        state.update { currentState ->
            currentState.copy(
                alarmHourOfDay = dateTime.hour,
                alarmMinute = dateTime.minute
            )
        }
    }

    fun onEvent(event: AddEditAlarmScreenUserEvent) {
        when (event) {
            is AddEditAlarmScreenUserEvent.AlarmTimeChanged -> {
                state.update { currentState ->
                    currentState.copy(
                        alarmHourOfDay = event.newAlarmHourOfDay,
                        alarmMinute = event.newAlarmMinute
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmEnabledChanged -> {
                state.update { currentState ->
                    currentState.copy(isAlarmEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmRepeatingScheduleDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isChooseAlarmRepeatingScheduleDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected -> {
                state.update { currentState ->
                    currentState.copy(
                        alarmRepeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper,
                        isChooseAlarmRepeatingScheduleDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmSnoozeConfigurationDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(
                        isChooseAlarmSnoozeConfigurationDialogVisible = event.isVisible
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected -> {
                state.update { currentState ->
                    currentState.copy(
                        alarmSnoozeConfigurationWrapper = event.newAlarmSnoozeConfigurationWrapper,
                        isChooseAlarmSnoozeConfigurationDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmRingtoneDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isChooseAlarmRingtoneDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmRingtoneSelected -> {
                alarmRingtonePlayer.stop()

                state.update { currentState ->
                    currentState.copy(
                        alarmRingtone = event.newAlarmRingtone,
                        isChooseAlarmRingtoneDialogVisible = false,
                        availableAlarmRingtonesWithPlaybackState =
                        currentState.availableAlarmRingtonesWithPlaybackState.mapValues { false }
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback -> {
                state.update { currentState ->
                    val isPlaying = currentState
                        .availableAlarmRingtonesWithPlaybackState[event.alarmRingtone]?.not()
                        ?: false

                    if (isPlaying) {
                        if (event.alarmRingtone == AlarmRingtone.CUSTOM_SOUND &&
                            (currentState.currentCustomAlarmRingtoneUri != null ||
                                    currentState.temporaryCustomAlarmRingtoneUri != null)
                        ) {
                            val alarmRingtoneUri = currentState.temporaryCustomAlarmRingtoneUri
                                ?: currentState.currentCustomAlarmRingtoneUri ?: return

                            alarmRingtonePlayer.playUriAlarmRingtonePreview(
                                alarmRingtoneUri = alarmRingtoneUri,
                                onPreviewCompleted = {
                                    state.update { currentState ->
                                        currentState.copy(
                                            availableAlarmRingtonesWithPlaybackState =
                                            currentState.availableAlarmRingtonesWithPlaybackState
                                                .mapValues { false }
                                        )
                                    }
                                }
                            )
                        } else {
                            alarmRingtonePlayer.playOriginalAlarmRingtonePreview(
                                alarmRingtone = event.alarmRingtone,
                                onPreviewCompleted = {
                                    state.update { currentState ->
                                        currentState.copy(
                                            availableAlarmRingtonesWithPlaybackState =
                                            currentState.availableAlarmRingtonesWithPlaybackState
                                                .mapValues { false }
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        alarmRingtonePlayer.stop()
                    }

                    currentState.copy(
                        availableAlarmRingtonesWithPlaybackState =
                        currentState.availableAlarmRingtonesWithPlaybackState.mapValues {
                            if (it.key == event.alarmRingtone) {
                                !it.value
                            } else {
                                false
                            }
                        }
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.CustomRingtoneUriRetrieved -> viewModelScope.launch {
                val uri = event.customRingtoneUri ?: run {
                    backendEventsChannel.send(
                        AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished(
                            isSuccess = false
                        )
                    )
                    return@launch
                }

                state.update { currentState ->
                    currentState.copy(
                        alarmRingtone = AlarmRingtone.CUSTOM_SOUND,
                        temporaryCustomAlarmRingtoneUri = uri
                    )
                }

                backendEventsChannel.send(
                    AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished(isSuccess = true)
                )
            }
            is AddEditAlarmScreenUserEvent.VibrationsEnabledChanged -> {
                state.update { currentState ->
                    currentState.copy(areVibrationsEnabled = event.areEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeEnabledChanged -> {
                state.update { currentState ->
                    currentState.copy(isCodeEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseGentleWakeUpDurationDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(
                        isChooseGentleWakeUpDurationDialogVisible = event.isVisible
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.GentleWakeUpDurationSelected -> {
                state.update { currentState ->
                    currentState.copy(
                        gentleWakeupDurationInSeconds = event.newGentleWakeUpDurationInSeconds,
                        isChooseGentleWakeUpDurationDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.TemporaryMuteEnabledChanged -> {
                state.update { currentState ->
                    currentState.copy(isTemporaryMuteEnabled = event.isEnabled)
                }
            }
            else -> { /* no-op */ }
        }
    }

    override fun onCleared() {
        alarmRingtonePlayer.onDestroy()

        super.onCleared()
    }
}