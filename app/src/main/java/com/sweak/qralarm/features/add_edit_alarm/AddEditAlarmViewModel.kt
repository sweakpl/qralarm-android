package com.sweak.qralarm.features.add_edit_alarm

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AddOrEditAlarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.alarm.DeleteAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.OptimizationGuideState
import com.sweak.qralarm.core.ui.convertAlarmRepeatingMode
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AddEditAlarmScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AdvancedAlarmSettingsScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmDraftMapper
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmScheduleCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

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
    private val alarmScheduleCalculator: AlarmScheduleCalculator,
    private val alarmDraftMapper: AlarmDraftMapper
) : ViewModel() {

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
    private var hasAlarmSaveStarted = false

    init {
        var launchedFromSavedState = false
        var initialDefaultCodeUpdate = true

        viewModelScope.launch {
            val savedStateBundle = savedStateHandle.get<Bundle?>(
                key = SAVED_STATE_HANDLE_BUNDLE_KEY
            )
            val savedState: AddEditAlarmFlowState? = savedStateBundle?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(
                        ADD_EDIT_ALARM_FLOW_STATE_KEY,
                        AddEditAlarmFlowState::class.java
                    )
                } else {
                    it.getParcelable(ADD_EDIT_ALARM_FLOW_STATE_KEY)
                }
            }

            if (savedStateBundle != null && savedState != null) {
                _state.update { savedState }
                hasUnsavedChanges = savedStateBundle.getBoolean(HAS_UNSAVED_CHANGES_KEY)
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

                val loadDraft: (AlarmDraft) -> AlarmDraft

                if (idOfAlarm == 0L) {
                    loadDraft = { currentDraft ->
                        alarmDraftMapper.withNewAlarm(currentDraft = currentDraft)
                    }
                } else {
                    val alarm = alarmsRepository.getAlarm(alarmId = idOfAlarm) ?: return@launch
                    val alarmRepeatingScheduleWrapper = convertAlarmRepeatingMode(
                        repeatingMode = alarm.repeatingMode
                    ) ?: return@launch
                    val isSystemDefaultAlarmRingtoneAvailable =
                        alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()

                    loadDraft = { currentDraft ->
                        alarmDraftMapper.withExistingAlarm(
                            currentDraft = currentDraft,
                            alarm = alarm,
                            alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                            isSystemDefaultAlarmRingtoneAvailable =
                                isSystemDefaultAlarmRingtoneAvailable
                        )
                    }
                }

                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isEditingExistingAlarm = idOfAlarm != 0L,
                        draft = loadDraft(currentState.draft),
                        availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState,
                        previouslySavedCodes = allSavedAlarmCodes
                    ).withRefreshedAlarmCountdown()
                }
            }
        }

        viewModelScope.launch {
            userDataRepository.setTemporaryScannedCode(
                if (launchedFromSavedState) state.value.draft.temporaryAssignedCode?.value
                else if (idOfAlarm == 0L) codesRepository.getDefaultAlarmCodeFlow().first()?.value
                else null
            )

            userDataRepository.temporaryScannedCode.collect { temporaryScannedCode ->
                if (launchedFromSavedState) {
                    launchedFromSavedState = false
                    initialDefaultCodeUpdate = false
                } else {
                    val existingCode = state.value.previouslySavedCodes
                        .find { it.value == temporaryScannedCode }

                    updateDraft { draft ->
                        draft.copy(
                            temporaryAssignedCode = temporaryScannedCode?.let {
                                Code(
                                    id = existingCode?.id ?: 0L,
                                    value = it,
                                    name = existingCode?.name
                                )
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

        savedStateHandle.setSavedStateProvider(SAVED_STATE_HANDLE_BUNDLE_KEY) {
            Bundle().apply {
                putParcelable(ADD_EDIT_ALARM_FLOW_STATE_KEY, state.value)
                putBoolean(HAS_UNSAVED_CHANGES_KEY, hasUnsavedChanges)
            }
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
                        with(currentState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                setAlarm(currentState.draft)
                                _state.update {
                                    it.copy(
                                        permissionsDialogState =
                                            AddEditAlarmFlowState.PermissionsDialogState(isVisible = false)
                                    )
                                }
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

                    if ((!event.cameraPermissionStatus && currentState.draft.isCodeEnabled) ||
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
                                            if (!event.cameraPermissionStatus &&
                                                currentState.draft.isCodeEnabled
                                            ) false else null,
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

                    setAlarm(currentState.draft)
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
                nextAlarmTimeUpdateJob?.cancel()
                nextAlarmTimeUpdateJob = viewModelScope.launch {
                    // Only debounce when the update comes from spinner which can be rapid:
                    if (event.source == AddEditAlarmScreenUserEvent.AlarmTimeChanged.Source.Spinner) {
                        delay(500.milliseconds)
                    }

                    updateDraft { draft ->
                        draft.copy(
                            alarmHourOfDay = event.newAlarmHourOfDay,
                            alarmMinute = event.newAlarmMinute
                        )
                    }

                    updateDraft(countsAsUnsavedChange = false) { draft ->
                        draft.copy(
                            onlyOnceAlarmDateInMillis =
                                alarmScheduleCalculator.resolveOnlyOnceAlarmDateInMillis(
                                    currentDateInMillis = draft.onlyOnceAlarmDateInMillis,
                                    hourOfDay = event.newAlarmHourOfDay,
                                    minute = event.newAlarmMinute,
                                    isDateSticky = draft.isOnlyOnceAlarmDateSticky
                                )
                        )
                    }

                    _state.update { currentState ->
                        currentState
                            .copy(isDialerPickerDialogVisible = false)
                            .withRefreshedAlarmCountdown()
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
                    .atTime(
                        state.value.draft.alarmHourOfDay ?: 0,
                        state.value.draft.alarmMinute ?: 0
                    )
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val isSticky = selectedDate == LocalDate.now(ZoneId.systemDefault())

                updateDraft { draft ->
                    draft.copy(
                        onlyOnceAlarmDateInMillis = newOnlyOnceDateInMillis,
                        isOnlyOnceAlarmDateSticky = isSticky
                    )
                }

                _state.update { currentState ->
                    currentState
                        .copy(isDatePickerDialogVisible = false)
                        .withRefreshedAlarmCountdown()
                }
            }

            is AddEditAlarmScreenUserEvent.ChooseAlarmRepeatingScheduleDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseAlarmRepeatingScheduleDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected -> {
                updateDraft { draft ->
                    draft.copy(
                        alarmRepeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper
                    )
                }

                _state.update { currentState ->
                    currentState
                        .copy(isChooseAlarmRepeatingScheduleDialogVisible = false)
                        .withRefreshedAlarmCountdown()
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
                updateDraft { draft ->
                    draft.copy(snoozeNumberToDurationPair = event.newSnoozeNumberToDurationPair)
                }

                _state.update { currentState ->
                    currentState.copy(isChooseAlarmSnoozeConfigurationDialogVisible = false)
                }
            }

            is AddEditAlarmScreenUserEvent.ChooseAlarmRingtoneDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseAlarmRingtoneConfigDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmRingtoneConfigSelected -> {
                alarmRingtonePlayer.stop()

                updateDraft { draft ->
                    draft.copy(
                        ringtone = event.newRingtone,
                        alarmVolumePercentage = event.newAlarmVolumePercentage
                    )
                }

                _state.update { currentState ->
                    currentState.copy(
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
                            (currentState.draft.currentCustomAlarmRingtoneUri != null ||
                                    currentState.draft.temporaryCustomAlarmRingtoneUri != null)
                        ) {
                            val alarmRingtoneUri =
                                currentState.draft.temporaryCustomAlarmRingtoneUri
                                    ?: currentState.draft.currentCustomAlarmRingtoneUri
                                    ?: return

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
                                it.key == event.ringtone && !it.value
                            }
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.CustomRingtoneUriRetrieved -> viewModelScope.launch {
                event.customRingtoneUri?.let { retrievedUri ->
                    updateDraft { draft ->
                        draft.copy(
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
                updateDraft { draft ->
                    draft.copy(areVibrationsEnabled = event.areEnabled)
                }
            }

            is AddEditAlarmScreenUserEvent.CodeEnabledChanged -> {
                updateDraft { draft ->
                    draft.copy(isCodeEnabled = event.isEnabled)
                }
            }

            is AddEditAlarmScreenUserEvent.AssignCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isAssignCodeDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.EditCodeNameDialogVisible -> {
                _state.update { currentState ->
                    val initialCodeName = if (event.isVisible) {
                        currentState.draft
                            .run { temporaryAssignedCode ?: currentlyAssignedCode }
                            ?.name
                    } else null

                    currentState.copy(
                        editCodeNameDialogState = AddEditAlarmFlowState.EditCodeNameDialogState(
                            isVisible = event.isVisible,
                            initialCodeName = initialCodeName
                        )
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.CodeNameEdited -> {
                updateDraft { draft ->
                    val updatedCode = (draft.temporaryAssignedCode
                        ?: draft.currentlyAssignedCode)?.copy(name = event.newName)
                    draft.copy(
                        temporaryAssignedCode = if (draft.temporaryAssignedCode != null) updatedCode
                        else draft.temporaryAssignedCode,
                        currentlyAssignedCode = if (draft.temporaryAssignedCode == null) updatedCode
                        else draft.currentlyAssignedCode
                    )
                }

                _state.update { currentState ->
                    currentState.copy(
                        editCodeNameDialogState = AddEditAlarmFlowState.EditCodeNameDialogState()
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
                if (state.value.draft.currentlyAssignedCode != null &&
                    state.value.draft.temporaryAssignedCode == null
                ) {
                    updateDraft { draft ->
                        draft.copy(currentlyAssignedCode = null)
                    }
                } else {
                    viewModelScope.launch {
                        userDataRepository.setTemporaryScannedCode(null)
                    }
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.OpenCodeLinkEnabledChanged -> {
                updateDraft { draft ->
                    draft.copy(isOpenCodeLinkEnabled = event.isEnabled)
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
                updateDraft { draft ->
                    draft.copy(cancelLockDurationInMinutes = event.newCancelLockDurationInMinutes)
                }

                _state.update { currentState ->
                    currentState.copy(isChooseCancelLockDurationDialogVisible = false)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.EmergencyTaskEnabledChanged -> {
                updateDraft { draft ->
                    draft.copy(isEmergencyTaskEnabled = event.isEnabled)
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmLabelChanged -> {
                updateDraft { draft ->
                    draft.copy(
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
                updateDraft { draft ->
                    draft.copy(
                        gentleWakeupDurationInSeconds = event.newGentleWakeUpDurationInSeconds
                    )
                }

                _state.update { currentState ->
                    currentState.copy(isChooseGentleWakeUpDurationDialogVisible = false)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.ChooseTemporaryMuteDurationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isChooseTemporaryMuteDurationDialogVisible = event.isVisible)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.TemporaryMuteDurationSelected -> {
                updateDraft { draft ->
                    draft.copy(
                        temporaryMuteDurationInSeconds = event.newTemporaryMuteDurationInSeconds
                    )
                }

                _state.update { currentState ->
                    currentState.copy(isChooseTemporaryMuteDurationDialogVisible = false)
                }
            }

            is AddEditAlarmScreenUserEvent.DeleteAlarmDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDeleteAlarmDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.DeleteAlarm -> viewModelScope.launch {
                deleteAlarm(alarmId = idOfAlarm)
                backendEventsChannel.send(AddEditAlarmFlowBackendEvent.AlarmDeleted)
            }

            is AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDownloadCodeDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.RefreshAlarmCountdown -> {
                updateDraft(countsAsUnsavedChange = false) { draft ->
                    val isOnlyOnce =
                        draft.alarmRepeatingScheduleWrapper.alarmRepeatingMode == ONLY_ONCE
                    val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(draft)

                    if (isOnlyOnce && nextAlarmTimeInMillis != null) {
                        draft.copy(onlyOnceAlarmDateInMillis = nextAlarmTimeInMillis)
                    } else {
                        draft
                    }
                }

                _state.update { currentState ->
                    currentState.withRefreshedAlarmCountdown()
                }
            }

            else -> { /* no-op */
            }
        }
    }

    private fun updateDraft(
        countsAsUnsavedChange: Boolean = true,
        transform: (AlarmDraft) -> AlarmDraft
    ) {
        var hasDraftChanged = false

        _state.update { currentState ->
            val newDraft = transform(currentState.draft)

            hasDraftChanged = newDraft != currentState.draft
            currentState.copy(draft = newDraft)
        }

        if (countsAsUnsavedChange && hasDraftChanged) {
            hasUnsavedChanges = true
        }
    }

    private fun computeNextAlarmTimeInMillis(draft: AlarmDraft): Long? {
        return alarmScheduleCalculator.computeNextAlarmTimeInMillis(
            hourOfDay = draft.alarmHourOfDay ?: return null,
            minute = draft.alarmMinute ?: return null,
            repeatingScheduleWrapper = draft.alarmRepeatingScheduleWrapper,
            onlyOnceAlarmDateInMillis = draft.onlyOnceAlarmDateInMillis
        )
    }

    private fun AddEditAlarmFlowState.withRefreshedAlarmCountdown(): AddEditAlarmFlowState {
        val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(draft) ?: return this

        return copy(
            daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis)
        )
    }

    private suspend fun setAlarm(draft: AlarmDraft) {
        val alarmToSave = alarmDraftMapper.toAlarm(
            draft = draft,
            alarmId = idOfAlarm
        ) ?: return

        if (hasAlarmSaveStarted) return
        hasAlarmSaveStarted = true

        val optimizationGuideState = userDataRepository.optimizationGuideState.first()

        if (optimizationGuideState == OptimizationGuideState.NONE) {
            userDataRepository.setOptimizationGuideState(
                state = OptimizationGuideState.SHOULD_BE_SEEN
            )
        }

        val addOrEditAlarmResult = addOrEditAlarm(
            alarm = alarmToSave,
            temporaryCustomRingtoneContentUriString =
                draft.temporaryCustomAlarmRingtoneUri?.toString()
        )

        if (addOrEditAlarmResult is AddOrEditAlarm.Result.CustomRingtoneSaveFailed) {
            backendEventsChannel.send(
                AddEditAlarmFlowBackendEvent.CustomRingtoneRetrievalFinished(isSuccess = false)
            )
        }

        val alarmId = addOrEditAlarmResult.alarmId

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)

        val setAlarmResult = setAlarm(
            alarmId = alarmId,
            isReschedulingMissedAlarm = false
        )

        if (setAlarmResult is SetAlarm.Result.Success) {
            backendEventsChannel.send(AddEditAlarmFlowBackendEvent.AlarmSaved)
        }
    }

    override fun onCleared() {
        alarmRingtonePlayer.onDestroy()
    }

    companion object {
        private const val SAVED_STATE_HANDLE_BUNDLE_KEY = "savedStateHandleBundleKey"
        private const val ADD_EDIT_ALARM_FLOW_STATE_KEY = "addEditAlarmFlowState"
        private const val HAS_UNSAVED_CHANGES_KEY = "hasUnsavedChanges"
    }
}