package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AddOrEditAlarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.alarm.DeleteAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.OptimizationGuideState
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AddEditAlarmScreenUserEvent
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel(assistedFactory = AddEditAlarmViewModel.Factory::class)
class AddEditAlarmViewModel @AssistedInject constructor(
    @Assisted private val idOfAlarm: Long,
    @Assisted private val alarmDraftHandle: AlarmDraftHandle,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val userDataRepository: UserDataRepository,
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
        fun create(
            idOfAlarmToEdit: Long,
            alarmDraftHandle: AlarmDraftHandle
        ): AddEditAlarmViewModel
    }

    private val _state = MutableStateFlow(AddEditAlarmFlowState())
    val state: StateFlow<AddEditAlarmFlowState> = combine(
        _state,
        alarmDraftHandle.draft,
        alarmDraftHandle.previouslySavedCodes
    ) { state, draft, previouslySavedCodes ->
        withFlowData(state, draft, previouslySavedCodes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = withFlowData(
            state = _state.value,
            draft = alarmDraftHandle.draft.value,
            previouslySavedCodes = alarmDraftHandle.previouslySavedCodes.value
        )
    )

    private val currentDraft: AlarmDraft
        get() = alarmDraftHandle.draft.value

    private val backendEventsChannel = Channel<AddEditAlarmFlowBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var nextAlarmTimeUpdateJob: Job? = null
    private var hasAlarmSaveStarted = false

    init {
        var launchedFromSavedState = alarmDraftHandle.isRestoredFromSavedState
        var initialDefaultCodeUpdate = true

        viewModelScope.launch { completeLoading() }

        viewModelScope.launch {
            userDataRepository.setTemporaryScannedCode(
                if (launchedFromSavedState) currentDraft.temporaryAssignedCode?.value
                else if (idOfAlarm == 0L) codesRepository.getDefaultAlarmCodeFlow().first()?.value
                else null
            )

            userDataRepository.temporaryScannedCode.collect { temporaryScannedCode ->
                if (launchedFromSavedState) {
                    launchedFromSavedState = false
                    initialDefaultCodeUpdate = false
                } else {
                    val existingCode = alarmDraftHandle.previouslySavedCodes.value
                        .find { it.value == temporaryScannedCode }

                    alarmDraftHandle.updateDraft { draft ->
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
    }

    private suspend fun completeLoading() {
        val availableRingtonesWithPlaybackState = Ringtone.entries
            .filter {
                it != Ringtone.SYSTEM_DEFAULT ||
                        alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()
            }
            .associateWith { false }

        alarmDraftHandle.isLoading.first { isLoading -> !isLoading }

        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                isEditingExistingAlarm = idOfAlarm != 0L,
                availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState
            ).withRefreshedAlarmCountdown()
        }
    }

    fun onEvent(event: AddEditAlarmFlowUserEvent) {
        when (event) {
            is AddEditAlarmScreenUserEvent.OnCancelClicked -> {
                if (_state.value.isEditingExistingAlarm && alarmDraftHandle.hasUnsavedChanges) {
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

                    val currentState = _state.value

                    if (currentState.permissionsDialogState.isVisible) {
                        with(currentState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                setAlarm(currentDraft)
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

                    if ((!event.cameraPermissionStatus && currentDraft.isCodeEnabled) ||
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
                                                currentDraft.isCodeEnabled
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

                    setAlarm(currentDraft)
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

                    alarmDraftHandle.updateDraft { draft ->
                        draft.copy(
                            alarmHourOfDay = event.newAlarmHourOfDay,
                            alarmMinute = event.newAlarmMinute
                        )
                    }

                    alarmDraftHandle.updateDraft(countsAsUnsavedChange = false) { draft ->
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
                        currentDraft.alarmHourOfDay ?: 0,
                        currentDraft.alarmMinute ?: 0
                    )
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val isSticky = selectedDate == LocalDate.now(ZoneId.systemDefault())

                alarmDraftHandle.updateDraft { draft ->
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
                alarmDraftHandle.updateDraft { draft ->
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
                alarmDraftHandle.updateDraft { draft ->
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

                alarmDraftHandle.updateDraft { draft ->
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
                            (currentDraft.currentCustomAlarmRingtoneUri != null ||
                                    currentDraft.temporaryCustomAlarmRingtoneUri != null)
                        ) {
                            val alarmRingtoneUri =
                                currentDraft.temporaryCustomAlarmRingtoneUri
                                    ?: currentDraft.currentCustomAlarmRingtoneUri
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
                    alarmDraftHandle.updateDraft { draft ->
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
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(areVibrationsEnabled = event.areEnabled)
                }
            }

            is AddEditAlarmScreenUserEvent.CodeEnabledChanged -> {
                alarmDraftHandle.updateDraft { draft ->
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
                        currentDraft
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
                alarmDraftHandle.updateDraft { draft ->
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
                if (currentDraft.currentlyAssignedCode != null &&
                    currentDraft.temporaryAssignedCode == null
                ) {
                    alarmDraftHandle.updateDraft { draft ->
                        draft.copy(currentlyAssignedCode = null)
                    }
                } else {
                    viewModelScope.launch {
                        userDataRepository.setTemporaryScannedCode(null)
                    }
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmLabelChanged -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(
                        alarmLabel = event.newAlarmLabel.run { this.ifBlank { null } }
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
                backendEventsChannel.send(AddEditAlarmFlowBackendEvent.AlarmDeleted)
            }

            is AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isDownloadCodeDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.RefreshAlarmCountdown -> {
                alarmDraftHandle.updateDraft(countsAsUnsavedChange = false) { draft ->
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

    private fun computeNextAlarmTimeInMillis(draft: AlarmDraft): Long? {
        return alarmScheduleCalculator.computeNextAlarmTimeInMillis(
            hourOfDay = draft.alarmHourOfDay ?: return null,
            minute = draft.alarmMinute ?: return null,
            repeatingScheduleWrapper = draft.alarmRepeatingScheduleWrapper,
            onlyOnceAlarmDateInMillis = draft.onlyOnceAlarmDateInMillis
        )
    }

    private fun AddEditAlarmFlowState.withRefreshedAlarmCountdown(): AddEditAlarmFlowState {
        val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(currentDraft) ?: return this

        return copy(
            daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(nextAlarmTimeInMillis)
        )
    }

    private fun withFlowData(
        state: AddEditAlarmFlowState,
        draft: AlarmDraft,
        previouslySavedCodes: List<Code>
    ): AddEditAlarmFlowState {
        return state.copy(draft = draft, previouslySavedCodes = previouslySavedCodes)
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
}