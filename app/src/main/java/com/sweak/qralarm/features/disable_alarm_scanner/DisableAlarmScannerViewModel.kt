package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.ID_OF_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisableAlarmScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private var assignedCode: String? = null
    private var shouldScan = true

    private val backendEventsChannel = Channel<DisableAlarmScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarm(alarmId = idOfAlarm)?.let {
                assignedCode = it.assignedCode
            }
        }
    }

    fun onEvent(event: DisableAlarmScannerScreenUserEvent) {
        when (event) {
            is DisableAlarmScannerScreenUserEvent.CodeResultScanned -> {
                if (shouldScan) {
                    shouldScan = false

                    viewModelScope.launch {
                        if (assignedCode == null) {
                            backendEventsChannel.send(
                                DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned
                            )
                        } else {
                            if (event.result.text == assignedCode) {
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
}