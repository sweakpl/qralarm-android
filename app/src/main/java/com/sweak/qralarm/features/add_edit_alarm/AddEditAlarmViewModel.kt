package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.model.Alarm.Ringtone
import com.sweak.qralarm.core.domain.user.UserDataRepository
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
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val userDataRepository: UserDataRepository,
    private val qrAlarmManager: QRAlarmManager
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

        viewModelScope.launch {
            userDataRepository.setTemporaryScannedCode(null)

            userDataRepository.temporaryScannedCode.collect { temporaryScannedCode ->
                state.update { currentState ->
                    currentState.copy(currentlyAssignedCode = temporaryScannedCode)
                }

                if (temporaryScannedCode != null) {
                    backendEventsChannel.send(
                        AddEditAlarmScreenBackendEvent.CustomCodeAssignmentFinished
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditAlarmScreenUserEvent) {
        when (event) {
            is AddEditAlarmScreenUserEvent.TrySaveAlarm -> {
                state.update { currentState ->
                    if (currentState.permissionsDialogState.isVisible) {
                        // You can also check here if all permissions are granted and just
                        // set the alarm and exit the screen.
                        return@update currentState.copy(
                            permissionsDialogState =
                            currentState.permissionsDialogState.copy(
                                cameraPermissionStatus =
                                currentState.permissionsDialogState.cameraPermissionStatus?.let {
                                    event.cameraPermissionStatus
                                },
                                notificationsPermissionState =
                                currentState.permissionsDialogState.notificationsPermissionState?.let {
                                    event.notificationsPermissionStatus
                                },
                                alarmsPermissionState =
                                currentState.permissionsDialogState.alarmsPermissionState?.let {
                                    qrAlarmManager.canScheduleExactAlarms()
                                },
                                fullScreenIntentPermissionState =
                                currentState.permissionsDialogState.fullScreenIntentPermissionState?.let {
                                    qrAlarmManager.canUseFullScreenIntent()
                                }
                            )
                        )
                    }

                    if (!event.cameraPermissionStatus ||
                        !event.notificationsPermissionStatus ||
                        !qrAlarmManager.canScheduleExactAlarms() ||
                        !qrAlarmManager.canUseFullScreenIntent()
                    ) {
                        return@update currentState.copy(
                            permissionsDialogState =
                                AddEditAlarmScreenState.PermissionsDialogState(
                                    isVisible = true,
                                    cameraPermissionStatus =
                                    if (!event.cameraPermissionStatus && currentState.isCodeEnabled)
                                        false else null,
                                    notificationsPermissionState =
                                    if (!event.notificationsPermissionStatus) false else null,
                                    alarmsPermissionState =
                                    if (!qrAlarmManager.canScheduleExactAlarms()) false else null,
                                    fullScreenIntentPermissionState =
                                    if (!qrAlarmManager.canUseFullScreenIntent()) false else null
                                )
                        )
                    }

                    return@update currentState
                }
            }
            is AddEditAlarmScreenUserEvent.HideMissingPermissionsDialog -> {
                state.update { currentState ->
                    currentState.copy(
                        permissionsDialogState = AddEditAlarmScreenState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(
                        isNotificationsPermissionDeniedDialogVisible = event.isVisible
                    )
                }
            }
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
                        alarmSnoozeMode = event.newAlarmSnoozeMode,
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
                        ringtone = event.newRingtone,
                        isChooseAlarmRingtoneDialogVisible = false,
                        availableRingtonesWithPlaybackState =
                        currentState.availableRingtonesWithPlaybackState.mapValues { false }
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback -> {
                state.update { currentState ->
                    val isPlaying = currentState
                        .availableRingtonesWithPlaybackState[event.ringtone]?.not()
                        ?: false

                    if (isPlaying) {
                        if (event.ringtone == Ringtone.CUSTOM_SOUND &&
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
                                            availableRingtonesWithPlaybackState =
                                            currentState.availableRingtonesWithPlaybackState
                                                .mapValues { false }
                                        )
                                    }
                                }
                            )
                        } else {
                            alarmRingtonePlayer.playOriginalAlarmRingtonePreview(
                                ringtone = event.ringtone,
                                onPreviewCompleted = {
                                    state.update { currentState ->
                                        currentState.copy(
                                            availableRingtonesWithPlaybackState =
                                            currentState.availableRingtonesWithPlaybackState
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
                        availableRingtonesWithPlaybackState =
                        currentState.availableRingtonesWithPlaybackState.mapValues {
                            if (it.key == event.ringtone) {
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
                        ringtone = Ringtone.CUSTOM_SOUND,
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
            is AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.ClearAssignedCode -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(null)
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