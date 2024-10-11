package com.sweak.qralarm.features.add_edit_alarm

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.navigation.ID_OF_ALARM_TO_EDIT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository,
    private val qrAlarmManager: QRAlarmManager
): ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM_TO_EDIT] ?: 0

    var state = MutableStateFlow(AddEditAlarmScreenState())

    private val backendEventsChannel = Channel<AddEditAlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        if (idOfAlarm == 0L) {
            val dateTime = ZonedDateTime.now()

            state.update { currentState ->
                currentState.copy(
                    alarmHourOfDay = dateTime.hour,
                    alarmMinute = dateTime.minute
                )
            }
        } else {
            viewModelScope.launch {
                val alarm = alarmsRepository.getAlarm(alarmId = idOfAlarm) ?: return@launch
                val alarmRepeatingScheduleWrapper = convertAlarmRepeatingMode(
                    repeatingMode = alarm.repeatingMode
                ) ?: return@launch

                state.update { currentState ->
                    currentState.copy(
                        alarmHourOfDay = alarm.alarmHourOfDay,
                        alarmMinute = alarm.alarmMinute,
                        isAlarmEnabled = alarm.isAlarmEnabled,
                        alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                        alarmSnoozeMode = alarm.snoozeMode,
                        ringtone = alarm.ringtone,
                        currentCustomAlarmRingtoneUri = alarm.customRingtoneUriString?.let {
                            Uri.parse(it)
                        },
                        areVibrationsEnabled = alarm.areVibrationsEnabled,
                        isCodeEnabled = alarm.isUsingCode,
                        currentlyAssignedCode = alarm.assignedCode,
                        gentleWakeupDurationInSeconds = alarm.gentleWakeUpDurationInSeconds,
                        isTemporaryMuteEnabled = alarm.isTemporaryMuteEnabled
                    )
                }
            }
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

    private fun convertAlarmRepeatingMode(
        repeatingMode: Alarm.RepeatingMode
    ): AlarmRepeatingScheduleWrapper? {
        if (repeatingMode is Alarm.RepeatingMode.Once) {
            return AlarmRepeatingScheduleWrapper(
                alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
            )
        } else if (repeatingMode is Alarm.RepeatingMode.Days) {
            val days = repeatingMode.repeatingDaysOfWeek

            if (days.size == 2 && days.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))) {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
                )
            } else if (days.size == 5 &&
                days.containsAll(
                    listOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )
                )
            ) {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
                )
            } else {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM,
                    alarmDaysOfWeek = days
                )
            }
        }

        return null
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

                    if (currentState.alarmHourOfDay != null && currentState.alarmMinute != null) {
                        viewModelScope.launch {
                            val repeatingMode =
                                when (currentState.alarmRepeatingScheduleWrapper.alarmRepeatingMode) {
                                    ONLY_ONCE -> {
                                        val onceAlarmDateTime = ZonedDateTime.now()
                                            .withHour(currentState.alarmHourOfDay)
                                            .withMinute(currentState.alarmMinute)
                                            .withSecond(0)
                                            .withNano(0)
                                            .run {
                                                if (isBefore(ZonedDateTime.now())) {
                                                    return@run plusDays(1)
                                                } else {
                                                    return@run this
                                                }
                                            }

                                        Alarm.RepeatingMode.Once(
                                            onceAlarmDateTime.toInstant().toEpochMilli()
                                        )
                                    }
                                    MON_FRI -> {
                                        Alarm.RepeatingMode.Days(
                                            listOf(
                                                DayOfWeek.MONDAY,
                                                DayOfWeek.TUESDAY,
                                                DayOfWeek.WEDNESDAY,
                                                DayOfWeek.THURSDAY,
                                                DayOfWeek.FRIDAY
                                            )
                                        )
                                    }
                                    SAT_SUN -> {
                                        Alarm.RepeatingMode.Days(
                                            listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                                        )
                                    }
                                    CUSTOM -> Alarm.RepeatingMode.Days(
                                        currentState.alarmRepeatingScheduleWrapper.alarmDaysOfWeek
                                    )
                                }
                            val customRingtoneUriString =
                                if (currentState.temporaryCustomAlarmRingtoneUri != null) {
                                    currentState.temporaryCustomAlarmRingtoneUri.toString()
                                } else if (currentState.currentCustomAlarmRingtoneUri != null) {
                                    currentState.currentCustomAlarmRingtoneUri.toString()
                                } else {
                                    null
                                }

                            alarmsRepository.addOrEditAlarm(
                                alarm = Alarm(
                                    alarmId = idOfAlarm,
                                    alarmHourOfDay = currentState.alarmHourOfDay,
                                    alarmMinute = currentState.alarmMinute,
                                    isAlarmEnabled = currentState.isAlarmEnabled,
                                    repeatingMode = repeatingMode,
                                    snoozeMode = currentState.alarmSnoozeMode,
                                    ringtone = currentState.ringtone,
                                    customRingtoneUriString = customRingtoneUriString,
                                    areVibrationsEnabled = currentState.areVibrationsEnabled,
                                    isUsingCode = currentState.isCodeEnabled,
                                    assignedCode = currentState.currentlyAssignedCode,
                                    gentleWakeUpDurationInSeconds =
                                    currentState.gentleWakeupDurationInSeconds,
                                    isTemporaryMuteEnabled = currentState.isTemporaryMuteEnabled
                                )
                            )

                            backendEventsChannel.send(AddEditAlarmScreenBackendEvent.AlarmSaved)
                        }
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