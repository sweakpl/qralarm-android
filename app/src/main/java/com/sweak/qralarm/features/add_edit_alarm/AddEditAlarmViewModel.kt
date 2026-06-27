package com.sweak.qralarm.features.add_edit_alarm

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AddOrEditAlarm
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.alarm.DeleteAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.OptimizationGuideState
import com.sweak.qralarm.core.ui.convertAlarmRepeatingMode
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.getEarliestOnlyOnceAlarmDate
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AddEditAlarmScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AdvancedAlarmSettingsScreenUserEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds
import com.sweak.qralarm.core.domain.alarm.Code as DomainCode

@HiltViewModel(assistedFactory = AddEditAlarmViewModel.Factory::class)
class AddEditAlarmViewModel @AssistedInject constructor(
    @Assisted private val idOfAlarm: Long,
    private val savedStateHandle: SavedStateHandle,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val addOrEditAlarm: AddOrEditAlarm,
    private val deleteAlarm: DeleteAlarm,
    private val setAlarm: SetAlarm,
    private val contentResolver: ContentResolver,
    private val filesDir: File
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(idOfAlarmToEdit: Long): AddEditAlarmViewModel
    }

    private var hasUnsavedChanges = false

    private var _state = MutableStateFlow(AddEditAlarmFlowState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<AddEditAlarmFlowBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var nextAlarmTimeUpdateJob: Job? = null

    init {
        var launchedFromSavedState = false
        var initialDefaultCodeUpdate = true

        viewModelScope.launch {
            val savedState = savedStateHandle.get<AddEditAlarmFlowState>(
                key = ADD_EDIT_ALARM_FLOW_STATE_KEY
            )

            if (savedState != null) {
                _state.update { savedState }
                launchedFromSavedState = true
            } else {
                val allSavedAlarmCodes = codesRepository.getCodesFlow()
                    .first()
                    .map { Code(id = it.codeId, value = it.value, name = it.name) }

                val availableRingtonesWithPlaybackState = Ringtone.entries
                    .filter {
                        it != Ringtone.SYSTEM_DEFAULT ||
                                alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()
                    }
                    .associateWith { false }

                if (idOfAlarm == 0L) {
                    val dateTime = ZonedDateTime.now().plusMinutes(1)
                    val defaultRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper()
                    val onlyOnceAlarmDateInMillis = resolveOnlyOnceAlarmDateInMillis(
                        currentDateInMillis = 0L,
                        hourOfDay = dateTime.hour,
                        minute = dateTime.minute,
                        isDateSticky = true
                    )
                    val daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(
                        alarmTimeInMillis = computeNextAlarmTimeInMillis(
                            hourOfDay = dateTime.hour,
                            minute = dateTime.minute,
                            repeatingScheduleWrapper = defaultRepeatingScheduleWrapper,
                            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis
                        )
                    )

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditingExistingAlarm = false,
                            alarmHourOfDay = dateTime.hour,
                            alarmMinute = dateTime.minute,
                            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
                            daysHoursAndMinutesUntilAlarm = daysHoursAndMinutesUntilAlarm,
                            alarmRepeatingScheduleWrapper = defaultRepeatingScheduleWrapper,
                            availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState,
                            previouslySavedCodes = allSavedAlarmCodes
                        )
                    }
                } else {
                    val alarm = alarmsRepository.getAlarm(alarmId = idOfAlarm) ?: return@launch
                    val alarmRepeatingScheduleWrapper = convertAlarmRepeatingMode(
                        repeatingMode = alarm.repeatingMode
                    ) ?: return@launch

                    val isDateSticky: Boolean
                    val onlyOnceAlarmDateInMillis: Long
                    val earliestDate = getEarliestOnlyOnceAlarmDate(
                        hourOfDay = alarm.alarmHourOfDay,
                        minute = alarm.alarmMinute
                    )

                    if (alarm.repeatingMode == Alarm.RepeatingMode.Once) {
                        val persistedDate = Instant.ofEpochMilli(alarm.nextAlarmTimeInMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        isDateSticky = !persistedDate.isAfter(earliestDate)
                        onlyOnceAlarmDateInMillis = resolveOnlyOnceAlarmDateInMillis(
                            currentDateInMillis = alarm.nextAlarmTimeInMillis,
                            hourOfDay = alarm.alarmHourOfDay,
                            minute = alarm.alarmMinute,
                            isDateSticky = isDateSticky
                        )
                    } else {
                        isDateSticky = true
                        onlyOnceAlarmDateInMillis = earliestDate
                            .atTime(alarm.alarmHourOfDay, alarm.alarmMinute)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }

                    val savedRingtone = if (alarm.ringtone == Ringtone.SYSTEM_DEFAULT &&
                        !alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()
                    ) {
                        Ringtone.GENTLE_GUITAR
                    } else {
                        alarm.ringtone
                    }

                    val daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(
                        alarmTimeInMillis = computeNextAlarmTimeInMillis(
                            hourOfDay = alarm.alarmHourOfDay,
                            minute = alarm.alarmMinute,
                            repeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis
                        )
                    )

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditingExistingAlarm = true,
                            alarmHourOfDay = alarm.alarmHourOfDay,
                            alarmMinute = alarm.alarmMinute,
                            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
                            isOnlyOnceAlarmDateSticky = isDateSticky,
                            daysHoursAndMinutesUntilAlarm = daysHoursAndMinutesUntilAlarm,
                            alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                            snoozeNumberToDurationPair = Pair(
                                first = alarm.snoozeConfig.snoozeMode.numberOfSnoozes,
                                second = alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes
                            ),
                            availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState,
                            ringtone = savedRingtone,
                            currentCustomAlarmRingtoneUri = alarm.customRingtoneUriString?.toUri(),
                            alarmVolumePercentage =
                                if (alarm.alarmVolumeMode is Alarm.AlarmVolumeMode.Custom) {
                                    alarm.alarmVolumeMode.volumePercentage
                                } else {
                                    null
                                },
                            areVibrationsEnabled = alarm.areVibrationsEnabled,
                            isCodeEnabled = alarm.isUsingCode,
                            previouslySavedCodes = allSavedAlarmCodes,
                            currentlyAssignedCode = alarm.assignedCode?.let {
                                Code(id = it.codeId, value = it.value, name = it.name)
                            },
                            isOpenCodeLinkEnabled = alarm.isOpenCodeLinkEnabled,
                            cancelLockDurationInMinutes = alarm.cancelLockDurationInMinutes,
                            isEmergencyTaskEnabled = alarm.isEmergencyTaskEnabled,
                            alarmLabel = alarm.alarmLabel,
                            gentleWakeupDurationInSeconds = alarm.gentleWakeUpDurationInSeconds,
                            temporaryMuteDurationInSeconds = alarm.temporaryMuteDurationInSeconds
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            userDataRepository.setTemporaryScannedCode(
                if (launchedFromSavedState) state.value.temporaryAssignedCode?.value
                else if (idOfAlarm == 0L) codesRepository.getDefaultAlarmCodeFlow().first()?.value
                else null
            )

            userDataRepository.temporaryScannedCode.collect { temporaryScannedCode ->
                if (launchedFromSavedState) {
                    launchedFromSavedState = false
                    initialDefaultCodeUpdate = false
                } else {
                    if (temporaryScannedCode != state.value.temporaryAssignedCode?.value) {
                        hasUnsavedChanges = true
                    }

                    val existingCode = state.value.previouslySavedCodes
                        .find { it.value == temporaryScannedCode }

                    _state.update { currentState ->
                        currentState.copy(
                            temporaryAssignedCode = temporaryScannedCode?.let {
                                Code(id = existingCode?.id ?: 0L, value = it, name = existingCode?.name)
                            }
                        )
                    }

                    if (temporaryScannedCode != null && !initialDefaultCodeUpdate) {
                        backendEventsChannel.send(
                            AddEditAlarmFlowBackendEvent.CustomCodeAssignmentFinished
                        )
                    }

                    initialDefaultCodeUpdate = false
                }
            }
        }

        viewModelScope.launch {
            state.collect { savedStateHandle[ADD_EDIT_ALARM_FLOW_STATE_KEY] = it }
        }
    }

    fun onEvent(event: AddEditAlarmFlowUserEvent) {
        when (event) {
            is AddEditAlarmScreenUserEvent.OnCancelClicked -> {
                if (state.value.isEditingExistingAlarm && hasUnsavedChanges) {
                    _state.update { currentState ->
                        currentState.copy(isDiscardAlarmChangesDialogVisible = true)
                    }
                } else {
                    viewModelScope.launch {
                        backendEventsChannel.send(
                            AddEditAlarmFlowBackendEvent.AlarmChangesDiscarded
                        )
                    }
                }
            }
            is AddEditAlarmScreenUserEvent.DiscardAlarmChangesDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDiscardAlarmChangesDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.TrySaveAlarm -> {
                viewModelScope.launch {
                    nextAlarmTimeUpdateJob?.join()

                    val currentState = state.value

                    if (currentState.permissionsDialogState.isVisible) {
                        with (currentState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                setAlarm(currentState)
                                _state.update { it.copy(
                                    permissionsDialogState =
                                    AddEditAlarmFlowState.PermissionsDialogState(isVisible = false)
                                ) }
                            } else {
                                _state.update {
                                    it.copy(
                                        permissionsDialogState =
                                            currentState.permissionsDialogState.copy(
                                                cameraPermissionState =
                                                    currentState.permissionsDialogState.cameraPermissionState?.let {
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
                            }
                        }
                        return@launch
                    }

                    if ((!event.cameraPermissionStatus && currentState.isCodeEnabled) ||
                        !event.notificationsPermissionStatus ||
                        !qrAlarmManager.canScheduleExactAlarms() ||
                        !qrAlarmManager.canUseFullScreenIntent()
                    ) {
                        _state.update {
                            it.copy(
                                permissionsDialogState =
                                    AddEditAlarmFlowState.PermissionsDialogState(
                                        isVisible = true,
                                        cameraPermissionState =
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
                        return@launch
                    }

                    setAlarm(currentState)
                }
            }
            is AddEditAlarmScreenUserEvent.HideMissingPermissionsDialog -> {
                _state.update { currentState ->
                    currentState.copy(
                        permissionsDialogState = AddEditAlarmFlowState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(
                        isNotificationsPermissionDeniedDialogVisible = event.isVisible
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.DialerPickerDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDialerPickerDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmTimeChanged -> {
                if (event.newAlarmHourOfDay != state.value.alarmHourOfDay ||
                    event.newAlarmMinute != state.value.alarmMinute
                ) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        alarmHourOfDay = event.newAlarmHourOfDay,
                        alarmMinute = event.newAlarmMinute,
                        isDialerPickerDialogVisible = false
                    )
                }

                nextAlarmTimeUpdateJob?.cancel()
                nextAlarmTimeUpdateJob = viewModelScope.launch {
                    delay(500.milliseconds)

                    _state.update { currentState ->
                        val newOnlyOnceDateInMillis = resolveOnlyOnceAlarmDateInMillis(
                            currentDateInMillis = currentState.onlyOnceAlarmDateInMillis,
                            hourOfDay = event.newAlarmHourOfDay,
                            minute = event.newAlarmMinute,
                            isDateSticky = currentState.isOnlyOnceAlarmDateSticky
                        )
                        val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(
                            hourOfDay = event.newAlarmHourOfDay,
                            minute = event.newAlarmMinute,
                            repeatingScheduleWrapper = currentState.alarmRepeatingScheduleWrapper,
                            onlyOnceAlarmDateInMillis = newOnlyOnceDateInMillis
                        )
                        currentState.copy(
                            onlyOnceAlarmDateInMillis = newOnlyOnceDateInMillis,
                            daysHoursAndMinutesUntilAlarm =
                                getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis)
                        )
                    }
                }
            }
            is AddEditAlarmScreenUserEvent.DatePickerDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDatePickerDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmDateSelected -> {
                val selectedDate = Instant.ofEpochMilli(event.selectedDateInMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val newOnlyOnceDateInMillis = selectedDate
                    .atTime(state.value.alarmHourOfDay ?: 0, state.value.alarmMinute ?: 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val isSticky = selectedDate == LocalDate.now(ZoneId.systemDefault())

                if (newOnlyOnceDateInMillis != state.value.onlyOnceAlarmDateInMillis) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(
                        hourOfDay = currentState.alarmHourOfDay ?: 0,
                        minute = currentState.alarmMinute ?: 0,
                        repeatingScheduleWrapper = currentState.alarmRepeatingScheduleWrapper,
                        onlyOnceAlarmDateInMillis = newOnlyOnceDateInMillis
                    )
                    currentState.copy(
                        onlyOnceAlarmDateInMillis = newOnlyOnceDateInMillis,
                        isOnlyOnceAlarmDateSticky = isSticky,
                        isDatePickerDialogVisible = false,
                        daysHoursAndMinutesUntilAlarm =
                            getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis)
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmRepeatingScheduleDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseAlarmRepeatingScheduleDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected -> {
                if (event.newAlarmRepeatingScheduleWrapper != state.value.alarmRepeatingScheduleWrapper) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(
                        hourOfDay = currentState.alarmHourOfDay ?: 0,
                        minute = currentState.alarmMinute ?: 0,
                        repeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper,
                        onlyOnceAlarmDateInMillis = currentState.onlyOnceAlarmDateInMillis
                    )
                    currentState.copy(
                        alarmRepeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper,
                        isChooseAlarmRepeatingScheduleDialogVisible = false,
                        daysHoursAndMinutesUntilAlarm =
                            getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis)
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmSnoozeConfigurationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(
                        isChooseAlarmSnoozeConfigurationDialogVisible = event.isVisible
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected -> {
                if (event.newSnoozeNumberToDurationPair != state.value.snoozeNumberToDurationPair) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        snoozeNumberToDurationPair = event.newSnoozeNumberToDurationPair,
                        isChooseAlarmSnoozeConfigurationDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ChooseAlarmRingtoneDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseAlarmRingtoneConfigDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmRingtoneConfigSelected -> {
                alarmRingtonePlayer.stop()

                if (event.newRingtone != state.value.ringtone ||
                    event.newAlarmVolumePercentage != state.value.alarmVolumePercentage
                ) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        ringtone = event.newRingtone,
                        alarmVolumePercentage = event.newAlarmVolumePercentage,
                        isChooseAlarmRingtoneConfigDialogVisible = false,
                        availableRingtonesWithPlaybackState =
                        currentState.availableRingtonesWithPlaybackState.mapValues { false }
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback -> {
                _state.update { currentState ->
                    val isPlaying = currentState
                        .availableRingtonesWithPlaybackState[event.ringtone] == true

                    if (!isPlaying) {
                        if (event.ringtone == Ringtone.CUSTOM_SOUND &&
                            (currentState.currentCustomAlarmRingtoneUri != null ||
                                    currentState.temporaryCustomAlarmRingtoneUri != null)
                        ) {
                            val alarmRingtoneUri = currentState.temporaryCustomAlarmRingtoneUri
                                ?: currentState.currentCustomAlarmRingtoneUri ?: return

                            alarmRingtonePlayer.playAlarmRingtonePreview(
                                alarmRingtoneUri = alarmRingtoneUri,
                                onPreviewCompleted = { hasErrorOccurred ->
                                    if (hasErrorOccurred) {
                                        viewModelScope.launch {
                                            backendEventsChannel.send(
                                                AddEditAlarmFlowBackendEvent
                                                    .AlarmRingtonePreviewPlaybackError
                                            )
                                        }
                                    }

                                    _state.update { currentState ->
                                        currentState.copy(
                                            availableRingtonesWithPlaybackState =
                                            currentState.availableRingtonesWithPlaybackState
                                                .mapValues { false }
                                        )
                                    }
                                }
                            )
                        } else {
                            alarmRingtonePlayer.playAlarmRingtonePreview(
                                ringtone = event.ringtone,
                                onPreviewCompleted = { hasErrorOccurred ->
                                    if (hasErrorOccurred) {
                                        viewModelScope.launch {
                                            backendEventsChannel.send(
                                                AddEditAlarmFlowBackendEvent
                                                    .AlarmRingtonePreviewPlaybackError
                                            )
                                        }
                                    }

                                    _state.update { currentState ->
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
                event.customRingtoneUri?.let { retrievedUri ->
                    hasUnsavedChanges = true

                    _state.update { currentState ->
                        currentState.copy(
                            ringtone = Ringtone.CUSTOM_SOUND,
                            temporaryCustomAlarmRingtoneUri = retrievedUri
                        )
                    }

                    backendEventsChannel.send(
                        AddEditAlarmFlowBackendEvent.CustomRingtoneRetrievalFinished(
                            isSuccess = true
                        )
                    )
                } ?: run {
                    backendEventsChannel.send(
                        AddEditAlarmFlowBackendEvent.CustomRingtoneRetrievalFinished(
                            isSuccess = false
                        )
                    )
                    return@launch
                }
            }
            is AddEditAlarmScreenUserEvent.VibrationsEnabledChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(areVibrationsEnabled = event.areEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeEnabledChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(isCodeEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.AssignCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isAssignCodeDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.EditCodeNameDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isEditCodeNameDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeNameEdited -> {
                val currentCodeName = (state.value.temporaryAssignedCode
                    ?: state.value.currentlyAssignedCode)?.name
                if (event.newName != currentCodeName) {
                    hasUnsavedChanges = true
                }
                _state.update { currentState ->
                    val updatedCode = (currentState.temporaryAssignedCode
                        ?: currentState.currentlyAssignedCode)?.copy(name = event.newName)
                    currentState.copy(
                        temporaryAssignedCode = if (currentState.temporaryAssignedCode != null) updatedCode
                            else currentState.temporaryAssignedCode,
                        currentlyAssignedCode = if (currentState.temporaryAssignedCode == null) updatedCode
                            else currentState.currentlyAssignedCode,
                        isEditCodeNameDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeChosenFromList -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(code = event.code.value)

                _state.update { currentState ->
                    currentState.copy(isAssignCodeDialogVisible = false)
                }
            }
            is AddEditAlarmScreenUserEvent.ClearAssignedCode -> {
                hasUnsavedChanges = true

                if (state.value.currentlyAssignedCode != null &&
                    state.value.temporaryAssignedCode == null
                ) {
                    _state.update { currentState ->
                        currentState.copy(currentlyAssignedCode = null)
                    }
                } else {
                    viewModelScope.launch {
                        userDataRepository.setTemporaryScannedCode(null)
                    }
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.OpenCodeLinkEnabledChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(isOpenCodeLinkEnabled = event.isEnabled)
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.ChooseCancelLockDurationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(
                        isChooseCancelLockDurationDialogVisible = event.isVisible
                    )
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.CancelLockDurationSelected -> {
                if (event.newCancelLockDurationInMinutes != state.value.cancelLockDurationInMinutes) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        cancelLockDurationInMinutes = event.newCancelLockDurationInMinutes,
                        isChooseCancelLockDurationDialogVisible = false
                    )
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.EmergencyTaskEnabledChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(isEmergencyTaskEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmLabelChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(
                        alarmLabel = event.newAlarmLabel.run { this.ifBlank { null } }
                    )
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.ChooseGentleWakeUpDurationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(
                        isChooseGentleWakeUpDurationDialogVisible = event.isVisible
                    )
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.GentleWakeUpDurationSelected -> {
                if (event.newGentleWakeUpDurationInSeconds != state.value.gentleWakeupDurationInSeconds) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        gentleWakeupDurationInSeconds = event.newGentleWakeUpDurationInSeconds,
                        isChooseGentleWakeUpDurationDialogVisible = false
                    )
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.ChooseTemporaryMuteDurationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseTemporaryMuteDurationDialogVisible = event.isVisible)
                }
            }
            is AdvancedAlarmSettingsScreenUserEvent.TemporaryMuteDurationSelected -> {
                if (event.newTemporaryMuteDurationInSeconds != state.value.temporaryMuteDurationInSeconds) {
                    hasUnsavedChanges = true
                }

                _state.update { currentState ->
                    currentState.copy(
                        temporaryMuteDurationInSeconds = event.newTemporaryMuteDurationInSeconds,
                        isChooseTemporaryMuteDurationDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.DeleteAlarmDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDeleteAlarmDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.DeleteAlarm -> viewModelScope.launch {
                deleteAlarm(alarmId = idOfAlarm)
                File(filesDir, idOfAlarm.toString()).apply {
                    if (exists()) delete()
                }
                backendEventsChannel.send(AddEditAlarmFlowBackendEvent.AlarmDeleted)
            }
            is AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDownloadCodeDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.RefreshAlarmCountdown -> {
                _state.update { currentState ->
                    val hourOfDay = currentState.alarmHourOfDay ?: return@update currentState
                    val minute = currentState.alarmMinute ?: return@update currentState
                    val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(
                        hourOfDay = hourOfDay,
                        minute = minute,
                        repeatingScheduleWrapper = currentState.alarmRepeatingScheduleWrapper,
                        onlyOnceAlarmDateInMillis = currentState.onlyOnceAlarmDateInMillis
                    )
                    val isOnlyOnce =
                        currentState.alarmRepeatingScheduleWrapper.alarmRepeatingMode == ONLY_ONCE

                    currentState.copy(
                        daysHoursAndMinutesUntilAlarm =
                            getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis),
                        onlyOnceAlarmDateInMillis = if (isOnlyOnce) nextAlarmTimeInMillis
                            else currentState.onlyOnceAlarmDateInMillis
                    )
                }
            }
            else -> { /* no-op */ }
        }
    }

    private suspend fun setAlarm(currentState: AddEditAlarmFlowState) {
        if (currentState.alarmHourOfDay == null || currentState.alarmMinute == null) {
            return
        }

        val optimizationGuideState = userDataRepository.optimizationGuideState.first()

        if (optimizationGuideState == OptimizationGuideState.NONE) {
            userDataRepository.setOptimizationGuideState(
                state = OptimizationGuideState.SHOULD_BE_SEEN
            )
        }

        val currentDateTime = ZonedDateTime.now()
        var alarmDateTime = ZonedDateTime.now()
            .withHour(currentState.alarmHourOfDay)
            .withMinute(currentState.alarmMinute)
            .withSecond(0)
            .withNano(0)
        val alarmTimeInMillis: Long

        val repeatingMode =
            if (currentState.alarmRepeatingScheduleWrapper.alarmRepeatingMode == ONLY_ONCE) {
                val chosenDate = Instant.ofEpochMilli(currentState.onlyOnceAlarmDateInMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                alarmDateTime = chosenDate
                    .atTime(currentState.alarmHourOfDay, currentState.alarmMinute)
                    .atZone(ZoneId.systemDefault())
                    .withSecond(0)
                    .withNano(0)

                if (alarmDateTime <= currentDateTime) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmTimeInMillis = alarmDateTime.toInstant().toEpochMilli()

                Alarm.RepeatingMode.Once
            } else {
                val repeatingDaysOfWeek =
                    when (currentState.alarmRepeatingScheduleWrapper.alarmRepeatingMode) {
                        MON_FRI -> listOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY
                        )
                        SAT_SUN -> listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                        EVERYDAY -> DayOfWeek.entries
                        CUSTOM -> currentState.alarmRepeatingScheduleWrapper.alarmDaysOfWeek
                    }

                while (alarmDateTime <= currentDateTime ||
                    alarmDateTime.dayOfWeek !in repeatingDaysOfWeek
                ) {
                    alarmDateTime = alarmDateTime.plusDays(1)
                }

                alarmTimeInMillis = alarmDateTime.toInstant().toEpochMilli()

                Alarm.RepeatingMode.Days(repeatingDaysOfWeek = repeatingDaysOfWeek)
            }

        val alarmToSave = Alarm(
            alarmId = idOfAlarm,
            alarmHourOfDay = currentState.alarmHourOfDay,
            alarmMinute = currentState.alarmMinute,
            isAlarmEnabled = true,
            isAlarmRunning = false,
            repeatingMode = repeatingMode,
            nextAlarmTimeInMillis = alarmTimeInMillis,
            snoozeConfig = Alarm.SnoozeConfig(
                snoozeMode = Alarm.SnoozeMode(
                    numberOfSnoozes = currentState.snoozeNumberToDurationPair.first,
                    snoozeDurationInMinutes = currentState.snoozeNumberToDurationPair.second
                ),
                numberOfSnoozesLeft = currentState.snoozeNumberToDurationPair.first,
                isAlarmSnoozed = false,
                nextSnoozedAlarmTimeInMillis = null
            ),
            ringtone = currentState.ringtone,
            customRingtoneUriString = currentState.currentCustomAlarmRingtoneUri?.toString(),
            alarmVolumeMode = if (currentState.alarmVolumePercentage != null) {
                Alarm.AlarmVolumeMode.Custom(
                    volumePercentage = currentState.alarmVolumePercentage
                )
            } else {
                Alarm.AlarmVolumeMode.System
            },
            areVibrationsEnabled = currentState.areVibrationsEnabled,
            isUsingCode = currentState.isCodeEnabled,
            assignedCode = (currentState.temporaryAssignedCode
                ?: currentState.currentlyAssignedCode)?.let {
                DomainCode(codeId = it.id, value = it.value, name = it.name)
            },
            isOpenCodeLinkEnabled = currentState.isOpenCodeLinkEnabled,
            cancelLockDurationInMinutes = currentState.cancelLockDurationInMinutes,
            isEmergencyTaskEnabled = currentState.isEmergencyTaskEnabled,
            alarmLabel = currentState.alarmLabel,
            gentleWakeUpDurationInSeconds = currentState.gentleWakeupDurationInSeconds,
            temporaryMuteDurationInSeconds = currentState.temporaryMuteDurationInSeconds,
            skipAlarmUntilTimeInMillis = null
        )

        val alarmId = addOrEditAlarm(alarm = alarmToSave).run {
            if (this > 0) this else idOfAlarm
        }

        if (currentState.temporaryCustomAlarmRingtoneUri != null) {
            try {
                val savedLocalAlarmSoundUri = withContext(Dispatchers.IO) {
                    copyUriContentToLocalStorage(
                        uri = currentState.temporaryCustomAlarmRingtoneUri,
                        alarmId = alarmId
                    )
                }

                alarmsRepository.setAlarmRingtoneUri(
                    alarmId = alarmId,
                    uri = savedLocalAlarmSoundUri.toString()
                )
            } catch (exception: Exception) {
                if (exception is IOException ||
                    exception is SecurityException ||
                    exception is NullPointerException
                ) {
                    backendEventsChannel.send(
                        AddEditAlarmFlowBackendEvent
                            .CustomRingtoneRetrievalFinished(isSuccess = false)
                    )
                } else {
                    throw exception
                }
            }
        }

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)

        val setAlarmResult = setAlarm(
            alarmId = alarmId,
            isReschedulingMissedAlarm = false
        )

        if (setAlarmResult is SetAlarm.Result.Success) {
            backendEventsChannel.send(AddEditAlarmFlowBackendEvent.AlarmSaved)
        }
    }

    @SuppressLint("SetWorldReadable")
    private fun copyUriContentToLocalStorage(uri: Uri, alarmId: Long): Uri {
        val file = File(filesDir, alarmId.toString())
        file.createNewFile()
        // Setting world-readable due to: https://stackoverflow.com/a/11977292/14037302
        file.setReadable(true, false)

        FileOutputStream(file).use { outputStream ->
            contentResolver.openInputStream(uri).use { inputStream ->
                if (inputStream == null) {
                    throw IOException()
                }

                copyStream(inputStream, outputStream)
                outputStream.flush()
            }
        }

        return Uri.fromFile(file)
    }

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int

        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    private fun computeNextAlarmTimeInMillis(
        hourOfDay: Int,
        minute: Int,
        repeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
        onlyOnceAlarmDateInMillis: Long
    ): Long {
        val currentDateTime = ZonedDateTime.now()
        var alarmDateTime = ZonedDateTime.now()
            .withHour(hourOfDay)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        return if (repeatingScheduleWrapper.alarmRepeatingMode == ONLY_ONCE) {
            val chosenDate = Instant.ofEpochMilli(onlyOnceAlarmDateInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            alarmDateTime = chosenDate
                .atTime(hourOfDay, minute)
                .atZone(ZoneId.systemDefault())
                .withSecond(0)
                .withNano(0)

            if (alarmDateTime <= currentDateTime) {
                alarmDateTime = alarmDateTime.plusDays(1)
            }

            alarmDateTime.toInstant().toEpochMilli()
        } else {
            val repeatingDaysOfWeek = when (repeatingScheduleWrapper.alarmRepeatingMode) {
                MON_FRI -> listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
                SAT_SUN -> listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                EVERYDAY -> DayOfWeek.entries
                CUSTOM -> repeatingScheduleWrapper.alarmDaysOfWeek
            }

            while (alarmDateTime <= currentDateTime ||
                alarmDateTime.dayOfWeek !in repeatingDaysOfWeek
            ) {
                alarmDateTime = alarmDateTime.plusDays(1)
            }

            alarmDateTime.toInstant().toEpochMilli()
        }
    }

    private fun resolveOnlyOnceAlarmDateInMillis(
        currentDateInMillis: Long,
        hourOfDay: Int,
        minute: Int,
        isDateSticky: Boolean = false
    ): Long {
        val earliestDate = getEarliestOnlyOnceAlarmDate(hourOfDay, minute)
        val date = if (isDateSticky) {
            earliestDate
        } else {
            Instant.ofEpochMilli(currentDateInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .let { if (it.isBefore(earliestDate)) earliestDate else it }
        }
        return date
            .atTime(hourOfDay, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    override fun onCleared() {
        alarmRingtonePlayer.onDestroy()

        super.onCleared()
    }
    
    companion object {
        private const val ADD_EDIT_ALARM_FLOW_STATE_KEY = "addEditAlarmFlowState"
    }
}