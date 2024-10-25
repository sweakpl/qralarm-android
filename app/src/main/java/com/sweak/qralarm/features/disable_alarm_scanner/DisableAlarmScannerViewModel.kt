package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.ID_OF_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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
    private lateinit var alarm: Alarm
    private var shouldScan = true

    private val backendEventsChannel = Channel<DisableAlarmScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

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
                        if (alarm.assignedCode == null) {
                            alarmsRepository.setAlarmSnoozed(
                                alarmId = idOfAlarm,
                                snoozed = false
                            )
                            handleAlarmRescheduling()

                            backendEventsChannel.send(
                                DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned
                            )
                        } else {
                            if (event.result.text == alarm.assignedCode) {
                                alarmsRepository.setAlarmSnoozed(
                                    alarmId = idOfAlarm,
                                    snoozed = false
                                )
                                handleAlarmRescheduling()

                                backendEventsChannel.send(
                                    DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned
                                )
                            } else {
                                shouldScan = true
                            }
                        }
                    }
                }
            }
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