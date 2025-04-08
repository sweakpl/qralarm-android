package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.ID_OF_ALARM
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.IS_DISABLING_BEFORE_ALARM_FIRED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisableAlarmScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private val isDisablingBeforeAlarmFired: Boolean =
        savedStateHandle[IS_DISABLING_BEFORE_ALARM_FIRED] ?: false

    private lateinit var alarm: Alarm
    private var shouldScan = true

    var state = MutableStateFlow(DisableAlarmScannerScreenState())

    private val backendEventsChannel = Channel<DisableAlarmScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var lastWrongCodeWarningMillis = 0L
    private val wrongCodeWarningDelayMillis = 3000L

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarm(alarmId = idOfAlarm)?.let {
                alarm = it
            }
        }
    }

    fun onEvent(event: DisableAlarmScannerScreenUserEvent) {
        when (event) {
            is DisableAlarmScannerScreenUserEvent.CodeResultScanned -> {
                if (shouldScan && ::alarm.isInitialized) {
                    shouldScan = false

                    viewModelScope.launch {
                        val scannedCodeText = event.codeResult

                        if (alarm.assignedCode == null) {
                            alarmsRepository.setAlarmSnoozed(
                                alarmId = idOfAlarm,
                                snoozed = false
                            )

                            if (isDisablingBeforeAlarmFired) {
                                disableAlarm(alarmId = alarm.alarmId)
                                alarmsRepository.setSkipNextAlarm(
                                    alarmId = alarm.alarmId,
                                    skip = true
                                )
                            }

                            handleAlarmRescheduling()

                            backendEventsChannel.send(
                                DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned(
                                    uriStringToOpen =
                                    if (alarm.isOpenCodeLinkEnabled) scannedCodeText else null
                                )
                            )
                        } else {
                            if (scannedCodeText == alarm.assignedCode) {
                                alarmsRepository.setAlarmSnoozed(
                                    alarmId = idOfAlarm,
                                    snoozed = false
                                )

                                if (isDisablingBeforeAlarmFired) {
                                    disableAlarm(alarmId = alarm.alarmId)
                                    alarmsRepository.setSkipNextAlarm(
                                        alarmId = alarm.alarmId,
                                        skip = true
                                    )
                                }

                                handleAlarmRescheduling()

                                backendEventsChannel.send(
                                    DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned(
                                        uriStringToOpen =
                                        if (alarm.isOpenCodeLinkEnabled) scannedCodeText else null
                                    )
                                )
                            } else {
                                val currentTimeInMillis = System.currentTimeMillis()

                                if (currentTimeInMillis - lastWrongCodeWarningMillis > wrongCodeWarningDelayMillis) {
                                    state.update { currentState ->
                                        currentState.copy(shouldShowIncorrectCodeWarning = true)
                                    }

                                    lastWrongCodeWarningMillis = currentTimeInMillis

                                    launch {
                                        delay(2500)

                                        state.update { currentState ->
                                            currentState.copy(
                                                shouldShowIncorrectCodeWarning = false
                                            )
                                        }
                                    }
                                }

                                shouldScan = true
                            }
                        }
                    }
                }
            }
            else -> { /* no-op */ }
        }
    }

    private suspend fun handleAlarmRescheduling() {
        if (::alarm.isInitialized) {
            if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                disableAlarm(alarmId = alarm.alarmId)
            } else if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                setAlarm(alarmId = alarm.alarmId)
            }
        }
    }
}