package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.ViewModel
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    private val alarmRingtonePlayer: AlarmRingtonePlayer
): ViewModel() {

    var state = MutableStateFlow(AddEditAlarmScreenState())

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