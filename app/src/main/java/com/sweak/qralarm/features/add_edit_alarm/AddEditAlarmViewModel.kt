package com.sweak.qralarm.features.add_edit_alarm

import android.content.ContentResolver
import android.net.Uri
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
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
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
    savedStateHandle: SavedStateHandle,
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

    var state = MutableStateFlow(AddEditAlarmScreenState())

    private val backendEventsChannel = Channel<AddEditAlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        if (idOfAlarm == 0L) {
            val dateTime = ZonedDateTime.now()

            state.update { currentState ->
                currentState.copy(
                    isEditingExistingAlarm = false,
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
                        isEditingExistingAlarm = true,
                        alarmHourOfDay = alarm.alarmHourOfDay,
                        alarmMinute = alarm.alarmMinute,
                        isAlarmEnabled = alarm.isAlarmEnabled,
                        alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                        alarmSnoozeMode = alarm.snoozeConfig.snoozeMode,
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
                    currentState.copy(temporaryAssignedCode = temporaryScannedCode)
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
            is AddEditAlarmScreenUserEvent.OnCancelClicked -> {
                if (state.value.isEditingExistingAlarm && hasUnsavedChanges) {
                    state.update { currentState ->
                        currentState.copy(isDiscardAlarmChangesDialogVisible = true)
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
                state.update { currentState ->
                    currentState.copy(isDiscardAlarmChangesDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.TrySaveAlarm -> {
                state.update { currentState ->
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
                                    AddEditAlarmScreenState.PermissionsDialogState(
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
                                AddEditAlarmScreenState.PermissionsDialogState(
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
                hasUnsavedChanges = true
                state.update { currentState ->
                    currentState.copy(
                        alarmHourOfDay = event.newAlarmHourOfDay,
                        alarmMinute = event.newAlarmMinute
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.AlarmEnabledChanged -> {
                hasUnsavedChanges = true
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
                hasUnsavedChanges = true
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
                hasUnsavedChanges = true
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
                hasUnsavedChanges = true
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

                            alarmRingtonePlayer.playAlarmRingtonePreview(
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
                            alarmRingtonePlayer.playAlarmRingtonePreview(
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
                event.customRingtoneUri?.let { retrievedUri ->
                    hasUnsavedChanges = true
                    state.update { currentState ->
                        currentState.copy(
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
                hasUnsavedChanges = true
                state.update { currentState ->
                    currentState.copy(areVibrationsEnabled = event.areEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.CodeEnabledChanged -> {
                hasUnsavedChanges = true
                state.update { currentState ->
                    currentState.copy(isCodeEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.ClearAssignedCode -> {
                hasUnsavedChanges = true

                if (state.value.currentlyAssignedCode != null &&
                    state.value.temporaryAssignedCode == null
                ) {
                    state.update { currentState ->
                        currentState.copy(currentlyAssignedCode = null)
                    }
                } else {
                    viewModelScope.launch {
                        userDataRepository.setTemporaryScannedCode(null)
                    }
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
                hasUnsavedChanges = true
                state.update { currentState ->
                    currentState.copy(
                        gentleWakeupDurationInSeconds = event.newGentleWakeUpDurationInSeconds,
                        isChooseGentleWakeUpDurationDialogVisible = false
                    )
                }
            }
            is AddEditAlarmScreenUserEvent.TemporaryMuteEnabledChanged -> {
                hasUnsavedChanges = true
                state.update { currentState ->
                    currentState.copy(isTemporaryMuteEnabled = event.isEnabled)
                }
            }
            is AddEditAlarmScreenUserEvent.DeleteAlarmDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isDeleteAlarmDialogVisible = event.isVisible)
                }
            }
            is AddEditAlarmScreenUserEvent.DeleteAlarm -> viewModelScope.launch {
                alarmsRepository.deleteAlarm(alarmId = idOfAlarm)
                File(filesDir, idOfAlarm.toString()).apply {
                    if (exists()) delete()
                }
                backendEventsChannel.send(AddEditAlarmScreenBackendEvent.AlarmDeleted)
            }
            else -> { /* no-op */ }
        }
    }

    private fun setAlarm(currentState: AddEditAlarmScreenState) {
        if (currentState.alarmHourOfDay != null && currentState.alarmMinute != null) {
            viewModelScope.launch {
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
                                CUSTOM -> currentState.alarmRepeatingScheduleWrapper.alarmDaysOfWeek
                                else -> emptyList()
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
                    isAlarmEnabled = currentState.isAlarmEnabled,
                    isAlarmRunning = false,
                    repeatingMode = repeatingMode,
                    nextAlarmTimeInMillis = alarmTimeInMillis,
                    snoozeConfig = Alarm.SnoozeConfig(
                        snoozeMode = currentState.alarmSnoozeMode,
                        numberOfSnoozesLeft = currentState.alarmSnoozeMode.numberOfSnoozes,
                        isAlarmSnoozed = false,
                        nextSnoozedAlarmTimeInMillis = null
                    ),
                    ringtone = currentState.ringtone,
                    customRingtoneUriString =
                    currentState.currentCustomAlarmRingtoneUri?.toString(),
                    areVibrationsEnabled = currentState.areVibrationsEnabled,
                    isUsingCode = currentState.isCodeEnabled,
                    assignedCode = currentState.temporaryAssignedCode
                        ?: currentState.currentlyAssignedCode,
                    gentleWakeUpDurationInSeconds =
                    currentState.gentleWakeupDurationInSeconds,
                    isTemporaryMuteEnabled = currentState.isTemporaryMuteEnabled
                )

                val alarmId = alarmsRepository.addOrEditAlarm(alarm = alarmToSave).run {
                    if (this > 0) this else idOfAlarm
                }

                if (currentState.temporaryCustomAlarmRingtoneUri != null) {
                    val savedLocalAlarmSoundUri = try {
                        copyUriContentToLocalStorage(
                            uri = currentState.temporaryCustomAlarmRingtoneUri,
                            alarmId = alarmId
                        )
                    } catch (exception: Exception) {
                        if (exception is IOException ||
                            exception is SecurityException
                        ) {
                            backendEventsChannel.send(
                                AddEditAlarmScreenBackendEvent
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

                var setAlarmResult: SetAlarm.Result? = null

                if (alarmToSave.isAlarmEnabled) {
                    setAlarmResult = setAlarm(alarmId = alarmId)
                } else {
                    disableAlarm(alarmId = alarmId)
                }

                setAlarmResult?.let { result ->
                    if (result is SetAlarm.Result.Success) {
                        backendEventsChannel.send(
                            AddEditAlarmScreenBackendEvent.AlarmSaved(
                                daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(
                                    alarmTimeInMillis = result.alarmTimInMillis
                                )
                            )
                        )
                    }
                } ?: run {
                    backendEventsChannel.send(AddEditAlarmScreenBackendEvent.AlarmSaved())
                }
            }
        }
    }

    private fun copyUriContentToLocalStorage(uri: Uri, alarmId: Long): Uri {
        val file = File(filesDir, alarmId.toString())
        file.createNewFile()

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
}