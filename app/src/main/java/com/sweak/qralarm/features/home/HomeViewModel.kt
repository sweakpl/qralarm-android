package com.sweak.qralarm.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.home.components.model.AlarmWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm
): ViewModel() {

    var state = MutableStateFlow(HomeScreenState())

    private val backendEventsChannel = Channel<HomeScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var isInitializing = true
    private var isTogglingAlarm = false

    private var currentlyToggledAlarmId: Long? = null
    private var currentlyToggledAlarmEnabledState: Boolean? = null

    init {
        viewModelScope.launch {
            alarmsRepository.getAllAlarms().collect { allAlarms ->
                if (!isInitializing && !isTogglingAlarm) {
                    // Delay added for the alarms list animation to be visible as the Flow update
                    // Comes while the HomeScreen is still hidden behind e.g. AddEditAlarmScreen:
                    delay(500)
                }

                state.update { currentState ->
                    currentState.copy(
                        alarmWrappers = allAlarms.mapNotNull { alarm ->
                            val alarmRepeatingScheduleWrapper =
                                convertAlarmRepeatingMode(alarm.repeatingMode)
                                    ?: return@mapNotNull null

                            AlarmWrapper(
                                alarmId = alarm.alarmId,
                                alarmHourOfDay = alarm.alarmHourOfDay,
                                alarmMinute = alarm.alarmMinute,
                                alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                                isAlarmEnabled = alarm.isAlarmEnabled,
                                isCodeEnabled = alarm.isUsingCode
                            )
                        }.sortedWith(
                            compareBy(AlarmWrapper::alarmHourOfDay, AlarmWrapper::alarmMinute)
                        )
                    )
                }

                isInitializing = false
                isTogglingAlarm = false
            }
        }
    }

    fun onEvent(event: HomeScreenUserEvent) {
        when (event) {
            is HomeScreenUserEvent.TryChangeAlarmEnabled -> {
                if (event.enabled == false) {
                    toggleAlarm(
                        alarmId = event.alarmId!!,
                        enabled = event.enabled
                    )

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
                                toggleAlarm(
                                    alarmId = currentlyToggledAlarmId!!,
                                    enabled = currentlyToggledAlarmEnabledState!!
                                )

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

                    val isCodeEnabled = currentState.alarmWrappers
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

                    toggleAlarm(
                        alarmId = currentlyToggledAlarmId!!,
                        enabled = currentlyToggledAlarmEnabledState!!
                    )

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
                setAlarmResult = setAlarm(alarmId = alarmId)
            } else {
                disableAlarm(alarmId = alarmId)
            }

            setAlarmResult?.let { result ->
                if (result is SetAlarm.Result.Success) {
                    backendEventsChannel.send(
                        HomeScreenBackendEvent.AlarmSet(
                            daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilAlarm(
                                alarmTimeInMillis = result.alarmTimInMillis
                            )
                        )
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
}