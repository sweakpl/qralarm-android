package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit

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
import com.sweak.qralarm.features.add_edit_alarm.AlarmDraftHandle
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmDraftMapper
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmScheduleCalculator
import com.sweak.qralarm.features.add_edit_alarm.util.RingtonePreviewController
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
    private val ringtonePreviewController: RingtonePreviewController,
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

    private val uiState = MutableStateFlow(AddEditAlarmUiState())

    val state: StateFlow<AddEditAlarmState> = combine(
        alarmDraftHandle.draft,
        alarmDraftHandle.previouslySavedCodes,
        ringtonePreviewController.playingRingtone,
        uiState
    ) { draft, previouslySavedCodes, playingRingtonePreview, uiState ->
        AddEditAlarmState(
            draft = draft,
            previouslySavedCodes = previouslySavedCodes,
            playingRingtonePreview = playingRingtonePreview,
            ui = uiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AddEditAlarmState(
            draft = alarmDraftHandle.draft.value,
            previouslySavedCodes = alarmDraftHandle.previouslySavedCodes.value,
            playingRingtonePreview = ringtonePreviewController.playingRingtone.value,
            ui = uiState.value
        )
    )

    private val currentDraft: AlarmDraft
        get() = alarmDraftHandle.draft.value

    private val backendEventsChannel = Channel<AddEditAlarmScreenBackendEvent>()
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
                            AddEditAlarmScreenBackendEvent.CustomCodeAssignmentFinished
                        )
                    }

                    initialDefaultCodeUpdate = false
                }
            }
        }
    }

    private suspend fun completeLoading() {
        val availableRingtones = ringtonePreviewController.getAvailableRingtones()

        alarmDraftHandle.isLoading.first { isLoading -> !isLoading }

        uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = false,
                isEditingExistingAlarm = idOfAlarm != 0L,
                availableRingtones = availableRingtones
            ).withRefreshedAlarmCountdown()
        }
    }

    fun onEvent(event: AddEditAlarmScreenUserEvent) {
        when (event) {
            is AddEditAlarmScreenUserEvent.OnCancelClicked -> {
                if (uiState.value.isEditingExistingAlarm && alarmDraftHandle.hasUnsavedChanges) {
                    uiState.update { currentUiState ->
                        currentUiState.copy(isDiscardAlarmChangesDialogVisible = true)
                    }
                } else {
                    viewModelScope.launch {
                        backendEventsChannel.send(
                            AddEditAlarmScreenBackendEvent.AlarmChangesDiscarded
                        )
                    }
                }
            }

            is AddEditAlarmScreenUserEvent.DiscardAlarmChangesDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isDiscardAlarmChangesDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.TrySaveAlarm -> {
                viewModelScope.launch {
                    nextAlarmTimeUpdateJob?.join()

                    val currentUiState = uiState.value

                    if (currentUiState.permissionsDialogState.isVisible) {
                        with(currentUiState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                setAlarm(currentDraft)
                                uiState.update {
                                    it.copy(
                                        permissionsDialogState =
                                            AddEditAlarmUiState.PermissionsDialogState(isVisible = false)
                                    )
                                }
                            } else {
                                uiState.update {
                                    it.copy(
                                        permissionsDialogState =
                                            currentUiState.permissionsDialogState.copy(
                                                cameraPermissionState =
                                                    currentUiState.permissionsDialogState.cameraPermissionState?.let {
                                                        event.cameraPermissionStatus
                                                    },
                                                notificationsPermissionState =
                                                    currentUiState.permissionsDialogState.notificationsPermissionState?.let {
                                                        event.notificationsPermissionStatus
                                                    },
                                                alarmsPermissionState =
                                                    currentUiState.permissionsDialogState.alarmsPermissionState?.let {
                                                        qrAlarmManager.canScheduleExactAlarms()
                                                    },
                                                fullScreenIntentPermissionState =
                                                    currentUiState.permissionsDialogState.fullScreenIntentPermissionState?.let {
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
                        uiState.update {
                            it.copy(
                                permissionsDialogState =
                                    AddEditAlarmUiState.PermissionsDialogState(
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
                uiState.update { currentUiState ->
                    currentUiState.copy(
                        permissionsDialogState = AddEditAlarmUiState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(
                        isNotificationsPermissionDeniedDialogVisible = event.isVisible
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.DialerPickerDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isDialerPickerDialogVisible = event.isVisible)
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

                    uiState.update { currentUiState ->
                        currentUiState
                            .copy(isDialerPickerDialogVisible = false)
                            .withRefreshedAlarmCountdown()
                    }
                }
            }

            is AddEditAlarmScreenUserEvent.DatePickerDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isDatePickerDialogVisible = event.isVisible)
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

                uiState.update { currentUiState ->
                    currentUiState
                        .copy(isDatePickerDialogVisible = false)
                        .withRefreshedAlarmCountdown()
                }
            }

            is AddEditAlarmScreenUserEvent.ChooseAlarmRepeatingScheduleDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseAlarmRepeatingScheduleDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(
                        alarmRepeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper
                    )
                }

                uiState.update { currentUiState ->
                    currentUiState
                        .copy(isChooseAlarmRepeatingScheduleDialogVisible = false)
                        .withRefreshedAlarmCountdown()
                }
            }

            is AddEditAlarmScreenUserEvent.ChooseAlarmSnoozeConfigurationDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(
                        isChooseAlarmSnoozeConfigurationDialogVisible = event.isVisible
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(snoozeNumberToDurationPair = event.newSnoozeNumberToDurationPair)
                }

                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseAlarmSnoozeConfigurationDialogVisible = false)
                }
            }

            is AddEditAlarmScreenUserEvent.ChooseAlarmRingtoneDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseAlarmRingtoneConfigDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.AlarmRingtoneConfigSelected -> {
                ringtonePreviewController.stop()

                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(
                        ringtone = event.newRingtone,
                        alarmVolumePercentage = event.newAlarmVolumePercentage
                    )
                }

                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseAlarmRingtoneConfigDialogVisible = false)
                }
            }

            is AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback -> {
                ringtonePreviewController.toggle(
                    ringtone = event.ringtone,
                    customRingtoneUri = currentDraft.temporaryCustomAlarmRingtoneUri
                        ?: currentDraft.currentCustomAlarmRingtoneUri,
                    onPlaybackError = {
                        viewModelScope.launch {
                            backendEventsChannel.send(
                                AddEditAlarmScreenBackendEvent.AlarmRingtonePreviewPlaybackError
                            )
                        }
                    }
                )
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
                        AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished(
                            isSuccess = true
                        )
                    )
                } ?: run {
                    backendEventsChannel.send(
                        AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished(
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
                uiState.update { currentUiState ->
                    currentUiState.copy(isAssignCodeDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.EditCodeNameDialogVisible -> {
                uiState.update { currentUiState ->
                    val initialCodeName = if (event.isVisible) {
                        currentDraft
                            .run { temporaryAssignedCode ?: currentlyAssignedCode }
                            ?.name
                    } else null

                    currentUiState.copy(
                        editCodeNameDialogState = AddEditAlarmUiState.EditCodeNameDialogState(
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

                uiState.update { currentUiState ->
                    currentUiState.copy(
                        editCodeNameDialogState = AddEditAlarmUiState.EditCodeNameDialogState()
                    )
                }
            }

            is AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.CodeChosenFromList -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(code = event.code.value)

                uiState.update { currentUiState ->
                    currentUiState.copy(isAssignCodeDialogVisible = false)
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
                uiState.update { currentUiState ->
                    currentUiState.copy(isDeleteAlarmDialogVisible = event.isVisible)
                }
            }

            is AddEditAlarmScreenUserEvent.DeleteAlarm -> viewModelScope.launch {
                deleteAlarm(alarmId = idOfAlarm)
                backendEventsChannel.send(AddEditAlarmScreenBackendEvent.AlarmDeleted)
            }

            is AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isDownloadCodeDialogVisible = event.isVisible)
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

                uiState.update { currentUiState ->
                    currentUiState.withRefreshedAlarmCountdown()
                }
            }

            is AddEditAlarmScreenUserEvent.ConfirmDiscardAlarmChanges,
            is AddEditAlarmScreenUserEvent.SaveAlarmClicked,
            is AddEditAlarmScreenUserEvent.RequestCameraPermission,
            is AddEditAlarmScreenUserEvent.RequestNotificationsPermission,
            is AddEditAlarmScreenUserEvent.RequestAlarmsPermission,
            is AddEditAlarmScreenUserEvent.RequestFullScreenIntentPermission,
            is AddEditAlarmScreenUserEvent.PickCustomRingtone,
            is AddEditAlarmScreenUserEvent.TryScanSpecificCode,
            is AddEditAlarmScreenUserEvent.GoToApplicationSettingsClicked,
            is AddEditAlarmScreenUserEvent.AdvancedSettingsClicked,
            is AddEditAlarmScreenUserEvent.SpecialSettingsClicked,
            is AddEditAlarmScreenUserEvent.AlarmsChainSettingsClicked,
            is AddEditAlarmScreenUserEvent.DownloadCode -> Unit
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

    private fun AddEditAlarmUiState.withRefreshedAlarmCountdown(): AddEditAlarmUiState {
        val nextAlarmTimeInMillis = computeNextAlarmTimeInMillis(currentDraft) ?: return this

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
                AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished(isSuccess = false)
            )
        }

        val alarmId = addOrEditAlarmResult.alarmId

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)

        val setAlarmResult = setAlarm(
            alarmId = alarmId,
            isReschedulingMissedAlarm = false
        )

        if (setAlarmResult is SetAlarm.Result.Success) {
            backendEventsChannel.send(AddEditAlarmScreenBackendEvent.AlarmSaved)
        }
    }

    override fun onCleared() {
        ringtonePreviewController.release()
    }
}