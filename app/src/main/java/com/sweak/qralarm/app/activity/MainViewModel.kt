package com.sweak.qralarm.app.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_REQUIRED_MATCHES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository,
    private val rescheduleAlarms: RescheduleAlarms
) : ViewModel() {

    private var _state = MutableStateFlow(MainActivityState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<MainActivityBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            rescheduleAlarms()
            migrateToNewEmergencyRequiredMatches()
            handleRatePromptTime()

            _state.update { currentState ->
                currentState.copy(
                    shouldShowSplashScreen = false,
                    isIntroductionFinished = userDataRepository.isIntroductionFinished.first()
                )
            }
        }
    }

    private suspend fun migrateToNewEmergencyRequiredMatches() {
        val currentEmergencyRequiredMatches = userDataRepository.emergencyRequiredMatches.first()

        if (currentEmergencyRequiredMatches !in EMERGENCY_AVAILABLE_REQUIRED_MATCHES) {
            userDataRepository.setEmergencyRequiredMatches(
                matches = EMERGENCY_AVAILABLE_REQUIRED_MATCHES.last()
            )
        }
    }

    private suspend fun handleRatePromptTime() {
        val nextRatePromptTimeInMillis = ZonedDateTime.now().plusDays(7).toInstant().toEpochMilli()

        if (userDataRepository.nextRatePromptTimeInMillis.first() == null) {
            userDataRepository.setNextRatePromptTimeInMillis(
                promptTime = nextRatePromptTimeInMillis
            )
        }

        _state.update { currentState ->
            currentState.copy(rateQRAlarmPromptTimeInMillis = nextRatePromptTimeInMillis)
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
            is MainActivityUserEvent.OnAlarmSaved -> {
                state.value.rateQRAlarmPromptTimeInMillis?.let {
                    if (it != 0L && it <= System.currentTimeMillis()) {
                        viewModelScope.launch {
                            backendEventsChannel.send(MainActivityBackendEvent.ShowRatePrompt)
                        }
                    }
                }
            }
        }
    }
}