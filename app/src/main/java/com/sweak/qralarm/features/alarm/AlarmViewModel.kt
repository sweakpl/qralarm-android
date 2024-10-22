package com.sweak.qralarm.features.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.features.alarm.navigation.ID_OF_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class AlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private var isUsingCode by Delegates.notNull<Boolean>()

    private val backendEventsChannel = Channel<AlarmScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarm(alarmId = idOfAlarm)?.let {
                isUsingCode = it.isUsingCode
            }
        }
    }

    fun onEvent(event: AlarmScreenUserEvent) {
        when (event) {
            AlarmScreenUserEvent.StopAlarmClicked -> {
                viewModelScope.launch {
                    backendEventsChannel.send(
                        if (isUsingCode) AlarmScreenBackendEvent.RequestCodeScanToStopAlarm
                        else AlarmScreenBackendEvent.StopAlarm
                    )
                }
            }
        }
    }
}