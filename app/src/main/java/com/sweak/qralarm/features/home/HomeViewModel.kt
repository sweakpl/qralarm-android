package com.sweak.qralarm.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CanManipulateAlarm
import com.sweak.qralarm.core.domain.alarm.CopyAlarm
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository.OptimizationGuideState
import com.sweak.qralarm.core.ui.convertAlarmRepeatingMode
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.features.home.components.model.AlarmWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val userDataRepository: UserDataRepository,
    private val rescheduleAlarms: RescheduleAlarms,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm,
    private val canManipulateAlarm: CanManipulateAlarm,
    private val copyAlarm: CopyAlarm,
    private val filesDir: File
): ViewModel() {

    var state = MutableStateFlow(HomeScreenState())

    private val backendEventsChannel = Channel<HomeScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var isTogglingAlarm = false
    private var justEditedAlarmId: Long? = null

    private var currentlyToggledAlarmId: Long? = null
    private var currentlyToggledAlarmEnabledState: Boolean? = null

    init {
        viewModelScope.launch {
            alarmsRepository.getAllAlarms().collect { allAlarms ->
                val (activeAlarms, nonActiveAlarms) = allAlarms.partition { it.isAlarmEnabled }
                var newlyEnabledAlarm: Alarm? = null

                if (justEditedAlarmId != null) {
                    // Delay added for the alarms list animation to be visible as the Flow update
                    // Comes while the HomeScreen is still hidden behind AddEditAlarmScreen:
                    delay(500)

                    if (justEditedAlarmId == -1L) {
                        val allPreviousAlarms =
                            state.value.activeAlarmWrappers + state.value.nonActiveAlarmWrappers

                        // Find the alarm in allAlarms that is not in the previousAlarms:
                        newlyEnabledAlarm = allAlarms.find { newAlarm ->
                            allPreviousAlarms.none { prevAlarm ->
                                prevAlarm.alarmId == newAlarm.alarmId
                            }
                        }
                    } else {
                        newlyEnabledAlarm = activeAlarms.find { it.alarmId == justEditedAlarmId }
                    }
                }

                state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        activeAlarmWrappers = convertToAlarmWrappers(activeAlarms),
                        nonActiveAlarmWrappers = convertToAlarmWrappers(nonActiveAlarms),
                        upcomingAlarmMessages = if (newlyEnabledAlarm != null) {
                            currentState.upcomingAlarmMessages + HomeScreenState.UpcomingAlarmMessage(
                                alarmContentHash = newlyEnabledAlarm.hashCode(),
                                alarmId = newlyEnabledAlarm.alarmId,
                                daysHoursAndMinutesUntilAlarm =
                                    getDaysHoursAndMinutesUntilAlarm(
                                        alarmTimeInMillis = newlyEnabledAlarm.nextAlarmTimeInMillis
                                    )
                            )
                        } else {
                            currentState.upcomingAlarmMessages
                        }
                    )
                }

                justEditedAlarmId = null
            }
        }

        viewModelScope.launch {
            val optimizationGuideState = userDataRepository.optimizationGuideState.first()

            if (optimizationGuideState != OptimizationGuideState.HAS_BEEN_SEEN) {
                viewModelScope.launch {
                    userDataRepository.optimizationGuideState.collect { optimizationGuideState ->
                        state.update { currentState ->
                            currentState.copy(
                                isOptimizationGuideDialogVisible =
                                optimizationGuideState == OptimizationGuideState.SHOULD_BE_SEEN
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            userDataRepository.isAlarmMissedDetected.collect { alarmMissedDetected ->
                if (alarmMissedDetected) {
                    state.update { currentState ->
                        currentState.copy(isAlarmMissedDialogVisible = true)
                    }
                }
            }
        }
    }

    private suspend fun convertToAlarmWrappers(alarms: List<Alarm>): List<AlarmWrapper> {
        return alarms.mapNotNull { alarm ->
            val alarmRepeatingScheduleWrapper =
                convertAlarmRepeatingMode(alarm.repeatingMode) ?: return@mapNotNull null

            return@mapNotNull AlarmWrapper(
                alarmId = alarm.alarmId,
                alarmHourOfDay = alarm.alarmHourOfDay,
                alarmMinute = alarm.alarmMinute,
                alarmLabel = alarm.alarmLabel,
                nextAlarmTimeInMillis = alarm.nextAlarmTimeInMillis,
                alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                isAlarmEnabled = alarm.isAlarmEnabled,
                isCodeEnabled = alarm.isUsingCode,
                skipNextAlarmConfig = AlarmWrapper.SkipNextAlarmConfig(
                    isSkippingSupported =
                        alarmRepeatingScheduleWrapper.alarmRepeatingMode != ONLY_ONCE &&
                                alarm.isAlarmEnabled,
                    isSkippingNextAlarm =
                        alarm.skipAlarmUntilTimeInMillis != null &&
                                alarm.skipAlarmUntilTimeInMillis > System.currentTimeMillis()
                ),
                isEmergencyAvailable =
                    alarm.isEmergencyTaskEnabled && !canManipulateAlarm(alarmId = alarm.alarmId)
            )
        }.sortedWith(
            compareBy(AlarmWrapper::alarmHourOfDay, AlarmWrapper::alarmMinute)
        )
    }

    fun onEvent(event: HomeScreenUserEvent) {
        when (event) {
            is HomeScreenUserEvent.AddNewAlarm -> viewModelScope.launch {
                justEditedAlarmId = -1L
                backendEventsChannel.send(HomeScreenBackendEvent.RedirectToAddEditAlarm())
            }
            is HomeScreenUserEvent.EditAlarmClicked -> viewModelScope.launch {
                if (canManipulateAlarm(alarmId = event.alarmId)) {
                    justEditedAlarmId = event.alarmId
                    backendEventsChannel.send(
                        HomeScreenBackendEvent.RedirectToAddEditAlarm(alarmId = event.alarmId)
                    )
                } else {
                    backendEventsChannel.send(HomeScreenBackendEvent.CanNotEditAlarm)
                }
            }
            is HomeScreenUserEvent.TryChangeAlarmEnabled -> {
                if (event.enabled == false) {
                    if (event.alarmId == null) return

                    viewModelScope.launch {
                        if (event.ignoreOneHourLock ||
                            canManipulateAlarm(alarmId = event.alarmId)
                        ) {
                            toggleAlarm(
                                alarmId = event.alarmId,
                                enabled = event.enabled
                            )
                        } else {
                            backendEventsChannel.send(
                                HomeScreenBackendEvent.CanNotDisableAlarm(alarmId = event.alarmId)
                            )
                        }
                    }

                    return
                }

                if (event.alarmId != null && event.enabled != null) {
                    currentlyToggledAlarmId = event.alarmId
                    currentlyToggledAlarmEnabledState = event.enabled
                } else {
                    if (currentlyToggledAlarmId == null || 
                        currentlyToggledAlarmEnabledState == null
                    ) {
                        state.update { currentState ->
                            currentState.copy(
                                permissionsDialogState = 
                                currentState.permissionsDialogState.copy(isVisible = false)
                            )
                        }
                        
                        return
                    }
                }

                state.update { currentState ->
                    if (currentState.permissionsDialogState.isVisible) {
                        with (currentState.permissionsDialogState) {
                            if ((cameraPermissionState == null || cameraPermissionState) &&
                                (alarmsPermissionState == null || alarmsPermissionState) &&
                                (notificationsPermissionState == null || notificationsPermissionState) &&
                                (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
                            ) {
                                currentlyToggledAlarmId?.let { alarmId ->
                                    currentlyToggledAlarmEnabledState?.let { enabled ->
                                        toggleAlarm(
                                            alarmId = alarmId,
                                            enabled = enabled
                                        )
                                    }
                                }

                                return@update currentState.copy(
                                    permissionsDialogState =
                                    currentState.permissionsDialogState.copy(isVisible = false)
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

                    val isCodeEnabled = currentState.nonActiveAlarmWrappers
                        .find { it.alarmId == event.alarmId }?.isCodeEnabled

                    if ((!event.cameraPermissionStatus && isCodeEnabled == true) ||
                        !event.notificationsPermissionStatus ||
                        !qrAlarmManager.canScheduleExactAlarms() ||
                        !qrAlarmManager.canUseFullScreenIntent()
                    ) {
                        return@update currentState.copy(
                            permissionsDialogState =
                            HomeScreenState.PermissionsDialogState(
                                isVisible = true,
                                cameraPermissionState =
                                if (!event.cameraPermissionStatus && isCodeEnabled == true)
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

                    currentlyToggledAlarmId?.let { alarmId ->
                        currentlyToggledAlarmEnabledState?.let { enabled ->
                            toggleAlarm(
                                alarmId = alarmId,
                                enabled = enabled
                            )
                        }
                    }

                    return@update currentState
                }
            }
            is HomeScreenUserEvent.HideMissingPermissionsDialog -> {
                state.update { currentState ->
                    currentState.copy(
                        permissionsDialogState = HomeScreenState.PermissionsDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is HomeScreenUserEvent.NotificationsPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(
                        isNotificationsPermissionDeniedDialogVisible = event.isVisible
                    )
                }
            }
            is HomeScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            is HomeScreenUserEvent.OptimizationGuideDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isOptimizationGuideDialogVisible = event.isVisible)
                }
            }
            is HomeScreenUserEvent.AlarmMissedDialogVisible -> viewModelScope.launch {
                userDataRepository.setAlarmMissedDetected(detected = false)

                state.update { currentState ->
                    currentState.copy(isAlarmMissedDialogVisible = event.isVisible)
                }
            }
            is HomeScreenUserEvent.TryDeleteAlarm -> viewModelScope.launch {
                if (canManipulateAlarm(alarmId = event.alarmId)) {
                    state.update { currentState ->
                        currentState.copy(
                            deleteAlarmDialogState = HomeScreenState.DeleteAlarmDialogState(
                                isVisible = true,
                                alarmId = event.alarmId
                            )
                        )
                    }
                } else {
                    backendEventsChannel.send(HomeScreenBackendEvent.CanNotEditAlarm)
                }
            }
            is HomeScreenUserEvent.HideDeleteAlarmDialog -> {
                state.update { currentState ->
                    currentState.copy(
                        deleteAlarmDialogState = HomeScreenState.DeleteAlarmDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is HomeScreenUserEvent.DeleteAlarm -> viewModelScope.launch {
                disableAlarm(alarmId = event.alarmId)
                alarmsRepository.deleteAlarm(alarmId = event.alarmId)
                File(filesDir, event.alarmId.toString()).apply {
                    if (exists()) delete()
                }

                state.update { currentState ->
                    currentState.copy(
                        deleteAlarmDialogState = HomeScreenState.DeleteAlarmDialogState(
                            isVisible = false
                        )
                    )
                }
            }
            is HomeScreenUserEvent.SkipNextAlarmChanged -> viewModelScope.launch {
                if (canManipulateAlarm(alarmId = event.alarmId)) {
                    alarmsRepository.setSkipNextAlarm(
                        alarmId = event.alarmId,
                        skip = event.skip
                    )
                    qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = event.alarmId)
                    rescheduleAlarms()
                } else {
                    backendEventsChannel.send(HomeScreenBackendEvent.CanNotEditAlarm)
                }
            }
            is HomeScreenUserEvent.CopyAlarm -> viewModelScope.launch {
                copyAlarm(event.alarmId)
            }
            is HomeScreenUserEvent.UpcomingAlarmMessageShown -> {
                state.update { currentState ->
                    currentState.copy(
                        upcomingAlarmMessages = currentState.upcomingAlarmMessages.filter {
                            it.alarmContentHash != event.alarmContentHash
                        }
                    )
                }
            }
            else -> { /* no-op */ }
        }
    }

    private fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        if (isTogglingAlarm) return
        
        viewModelScope.launch {
            isTogglingAlarm = true
            currentlyToggledAlarmId = null
            currentlyToggledAlarmEnabledState = null

            var setAlarmResult: SetAlarm.Result? = null

            if (enabled) {
                alarmsRepository.setSkipNextAlarm(
                    alarmId = alarmId,
                    skip = false
                )
                setAlarmResult = setAlarm(
                    alarmId = alarmId,
                    isReschedulingMissedAlarm = false
                )
            } else {
                disableAlarm(alarmId = alarmId)

                state.update { currentState ->
                    currentState.copy(
                        upcomingAlarmMessages =
                            currentState.upcomingAlarmMessages.filter {
                                it.alarmId != alarmId
                            }
                    )
                }
            }

            setAlarmResult?.let { result ->
                if (result is SetAlarm.Result.Success) {
                    val alarm = alarmsRepository.getAlarm(
                        alarmId = alarmId
                    ) ?: return@let

                    state.update { currentState ->
                        currentState.copy(
                            upcomingAlarmMessages = currentState.upcomingAlarmMessages +
                                    HomeScreenState.UpcomingAlarmMessage(
                                        alarmContentHash = alarm.hashCode(),
                                        alarmId = alarm.alarmId,
                                        daysHoursAndMinutesUntilAlarm =
                                            getDaysHoursAndMinutesUntilAlarm(
                                                alarmTimeInMillis = result.alarmTimInMillis
                                            )
                                    )
                        )
                    }
                }
            }
        }.invokeOnCompletion {
            isTogglingAlarm = false
        }
    }
}