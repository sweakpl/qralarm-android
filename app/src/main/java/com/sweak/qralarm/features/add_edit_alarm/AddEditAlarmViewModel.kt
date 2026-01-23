package com.sweak.qralarm.features.add_edit_alarm

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.OptimizationGuideState
import com.sweak.qralarm.core.ui.convertAlarmRepeatingMode
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AddEditAlarmScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AdvancedAlarmSettingsScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.navigation.ID_OF_ALARM_TO_EDIT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm,
    private val contentResolver: ContentResolver,
    private val filesDir: File
): ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM_TO_EDIT] ?: 0
    private var hasUnsavedChanges = false

    private var _state = MutableStateFlow(AddEditAlarmFlowState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<AddEditAlarmFlowBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

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
                val allSavedAlarmCodes = alarmsRepository.getAllAlarms()
                    .map { alarms ->
                        alarms
                            .mapNotNull { alarm -> alarm.assignedCode }
                            .let { codes ->
                                userDataRepository.defaultAlarmCode.first()?.let {
                                    codes + it
                                } ?: codes
                            }
                            .distinct()
                    }
                    .first()

                if (idOfAlarm == 0L) {
                    val dateTime = ZonedDateTime.now()

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditingExistingAlarm = false,
                            alarmHourOfDay = dateTime.hour,
                            alarmMinute = dateTime.minute,
                            previouslySavedCodes = allSavedAlarmCodes
                        )
                    }
                } else {
                    val alarm = alarmsRepository.getAlarm(alarmId = idOfAlarm) ?: return@launch
                    val alarmRepeatingScheduleWrapper = convertAlarmRepeatingMode(
                        repeatingMode = alarm.repeatingMode
                    ) ?: return@launch

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isEditingExistingAlarm = true,
                            alarmHourOfDay = alarm.alarmHourOfDay,
                            alarmMinute = alarm.alarmMinute,
                            alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                            snoozeNumberToDurationPair = Pair(
                                first = alarm.snoozeConfig.snoozeMode.numberOfSnoozes,
                                second = alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes
                            ),
                            ringtone = alarm.ringtone,
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
                            currentlyAssignedCode = alarm.assignedCode,
                            isOpenCodeLinkEnabled = alarm.isOpenCodeLinkEnabled,
                            isOneHourLockEnabled = alarm.isOneHourLockEnabled,
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
                if (launchedFromSavedState) state.value.temporaryAssignedCode
                else if (idOfAlarm == 0L) userDataRepository.defaultAlarmCode.first()
                else null
            )

            userDataRepository.temporaryScannedCode.collect { temporaryScannedCode ->
                if (launchedFromSavedState) {
                    launchedFromSavedState = false
                    initialDefaultCodeUpdate = false
                } else {
                    if (temporaryScannedCode != state.value.temporaryAssignedCode) {
                        hasUnsavedChanges = true
                    }

                    _state.update { currentState ->
                        currentState.copy(temporaryAssignedCode = temporaryScannedCode)
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
                _state.update { currentState ->
                    if (currentState.permissionsDialogState.isVisible) {
                        with (currentState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                setAlarm(currentState)

                                return@update currentState.copy(
                                    permissionsDialogState =
                                    AddEditAlarmFlowState.PermissionsDialogState(
                                        isVisible = false
                                    )
                                )
                            }
                        }

                        return@update currentState.copy(
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

                    if ((!event.cameraPermissionStatus && currentState.isCodeEnabled) ||
                        !event.notificationsPermissionStatus ||
                        !qrAlarmManager.canScheduleExactAlarms() ||
                        !qrAlarmManager.canUseFullScreenIntent()
                    ) {
                        return@update currentState.copy(
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

                    setAlarm(currentState)

                    return@update currentState
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
                    currentState.copy(
                        alarmRepeatingScheduleWrapper = event.newAlarmRepeatingScheduleWrapper,
                        isChooseAlarmRepeatingScheduleDialogVisible = false
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
            is AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeChosenFromList -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(code = event.code)

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
            is AdvancedAlarmSettingsScreenUserEvent.OneHourLockEnabledChanged -> {
                hasUnsavedChanges = true
                _state.update { currentState ->
                    currentState.copy(isOneHourLockEnabled = event.isEnabled)
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
                disableAlarm(alarmId = idOfAlarm)
                alarmsRepository.deleteAlarm(alarmId = idOfAlarm)
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
            else -> { /* no-op */ }
        }
    }

    private fun setAlarm(currentState: AddEditAlarmFlowState) {
        if (currentState.alarmHourOfDay == null || currentState.alarmMinute == null) {
            return
        }

        viewModelScope.launch {
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
                assignedCode = currentState.temporaryAssignedCode
                    ?: currentState.currentlyAssignedCode,
                isOpenCodeLinkEnabled = currentState.isOpenCodeLinkEnabled,
                isOneHourLockEnabled = currentState.isOneHourLockEnabled,
                isEmergencyTaskEnabled = currentState.isEmergencyTaskEnabled,
                alarmLabel = currentState.alarmLabel,
                gentleWakeUpDurationInSeconds = currentState.gentleWakeupDurationInSeconds,
                temporaryMuteDurationInSeconds = currentState.temporaryMuteDurationInSeconds,
                skipAlarmUntilTimeInMillis = null
            )

            val alarmId = alarmsRepository.addOrEditAlarm(alarm = alarmToSave).run {
                if (this > 0) this else idOfAlarm
            }

            if (currentState.temporaryCustomAlarmRingtoneUri != null) {
                val savedLocalAlarmSoundUri = try {
                    withContext(Dispatchers.IO) {
                        copyUriContentToLocalStorage(
                            uri = currentState.temporaryCustomAlarmRingtoneUri,
                            alarmId = alarmId
                        )
                    }
                } catch (exception: Exception) {
                    if (exception is IOException ||
                        exception is SecurityException
                    ) {
                        backendEventsChannel.send(
                            AddEditAlarmFlowBackendEvent
                                .CustomRingtoneRetrievalFinished(isSuccess = false)
                        )
                    } else {
                        throw exception
                    }
                }

                alarmsRepository.addOrEditAlarm(
                    alarm = alarmToSave.copy(
                        alarmId = alarmId,
                        customRingtoneUriString = savedLocalAlarmSoundUri.toString()
                    )
                )
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

    override fun onCleared() {
        alarmRingtonePlayer.onDestroy()

        super.onCleared()
    }
    
    companion object {
        private const val ADD_EDIT_ALARM_FLOW_STATE_KEY = "addEditAlarmFlowState"
    }
}