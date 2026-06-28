package com.sweak.qralarm.app.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import com.sweak.qralarm.core.domain.user.ShouldShowWhatsNew
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.WHATS_NEW_VERSION_CODE
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_REQUIRED_MATCHES
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_DEFAULT_REQUIRED_MATCHES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val rescheduleAlarms: RescheduleAlarms,
    private val shouldShowWhatsNew: ShouldShowWhatsNew
) : ViewModel() {

    private var _state = MutableStateFlow(MainActivityState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<MainActivityBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            rescheduleAlarms(rescheduleAlarmsIfMissedByFiveMinutes = false)
            codesRepository.migrateLegacyDefaultAlarmCode()
            migrateToNewEmergencyRequiredMatches()

            _state.update { currentState ->
                currentState.copy(
                    shouldShowSplashScreen = false,
                    isIntroductionFinished = userDataRepository.isIntroductionFinished.first(),
                    isWhatsNewDialogVisible = shouldShowWhatsNew()
                )
            }
        }

        viewModelScope.launch {
            userDataRepository.theme.collect { theme ->
                _state.update { currentState ->
                    currentState.copy(theme = theme)
                }
            }
        }
    }

    private suspend fun migrateToNewEmergencyRequiredMatches() {
        val currentEmergencyRequiredMatches = userDataRepository.emergencyRequiredMatches.first()

        if (currentEmergencyRequiredMatches !in EMERGENCY_AVAILABLE_REQUIRED_MATCHES) {
            userDataRepository.setEmergencyRequiredMatches(
                matches = EMERGENCY_DEFAULT_REQUIRED_MATCHES
            )
        }
    }

    fun onEvent(event: MainActivityUserEvent) {
        when (event) {
            is MainActivityUserEvent.ObserveActiveAlarms -> viewModelScope.launch {
                alarmsRepository.getAllAlarms().collect { alarms ->
                    alarms.firstOrNull { alarm ->
                        alarm.isAlarmRunning || alarm.snoozeConfig.isAlarmSnoozed
                    }?.let { activeAlarm ->
                        if (activeAlarm.isAlarmRunning && !AlarmService.isRunning) {
                            alarmsRepository.setAlarmRunning(
                                alarmId = activeAlarm.alarmId,
                                running = false
                            )
                            return@let
                        }

                        backendEventsChannel.send(
                            MainActivityBackendEvent.NavigateToActiveAlarm(
                                alarmId = activeAlarm.alarmId
                            )
                        )
                    }
                }
            }

            is MainActivityUserEvent.OnOnboardingFinished -> viewModelScope.launch {
                userDataRepository.setIntroductionFinished(finished = true)
            }

            is MainActivityUserEvent.OnWhatsNewDialogDismissed -> viewModelScope.launch {
                userDataRepository.setWhatsNewLastShownVersionCode(WHATS_NEW_VERSION_CODE)
                _state.update { it.copy(isWhatsNewDialogVisible = false) }
            }
        }
    }
}
