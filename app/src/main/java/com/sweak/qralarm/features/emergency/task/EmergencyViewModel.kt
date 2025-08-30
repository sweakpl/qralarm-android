package com.sweak.qralarm.features.emergency.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.emergency.task.navigation.ID_OF_ALARM_TO_CANCEL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val disableAlarm: DisableAlarm,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val idOfAlarmToCancel: Long = savedStateHandle[ID_OF_ALARM_TO_CANCEL] ?: 0

    private var _state = MutableStateFlow(EmergencyScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<EmergencyScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            _state.update { currentState ->
                val valueRange = userDataRepository.emergencySliderRange.first()
                val targetValue = Random.nextInt(range = valueRange)
                var currentValue = Random.nextInt(range = valueRange)

                while (currentValue == targetValue) {
                    // Ensure current value is not equal to target value
                    currentValue = Random.nextInt(range = valueRange)
                }

                currentState.copy(
                    emergencyTaskConfig = currentState.emergencyTaskConfig.copy(
                        valueRange = valueRange,
                        targetValue = targetValue,
                        currentValue = currentValue,
                        remainingMatches = userDataRepository.emergencyRequiredMatches.first(),
                        isCompleted = false
                    )
                )
            }
        }
    }

    fun onEvent(event: EmergencyScreenUserEvent) {
        when (event) {
            is EmergencyScreenUserEvent.OnTaskStarted -> {
                _state.update { currentState ->
                    currentState.copy(isTaskStarted = true)
                }
            }
            is EmergencyScreenUserEvent.OnTaskValueChanged -> {
                _state.update { currentState ->
                    currentState.copy(
                        emergencyTaskConfig = currentState.emergencyTaskConfig.copy(
                            currentValue = event.value
                        )
                    )
                }
            }
            is EmergencyScreenUserEvent.OnTaskValueSelected -> {
                val selectedValue = state.value.emergencyTaskConfig.currentValue
                val targetValue = state.value.emergencyTaskConfig.targetValue

                if (selectedValue == targetValue) {
                    val remainingMatches = state.value.emergencyTaskConfig.remainingMatches - 1

                    _state.update { currentState ->
                        currentState.copy(
                            emergencyTaskConfig =
                                if (remainingMatches <= 0) {
                                    viewModelScope.launch {
                                        if (idOfAlarmToCancel != 0L) {
                                            disableAlarm(idOfAlarmToCancel)
                                        }

                                        delay(1500)

                                        backendEventsChannel.send(
                                            EmergencyScreenBackendEvent.EmergencyTaskCompleted
                                        )
                                    }

                                    currentState.emergencyTaskConfig.copy(
                                        isCompleted = true,
                                        remainingMatches = 0
                                    )
                                } else {
                                    val currentValue = currentState.emergencyTaskConfig.currentValue
                                    var targetValue = Random.nextInt(
                                        range = currentState.emergencyTaskConfig.valueRange
                                    )

                                    while (targetValue == currentValue) {
                                        // Ensure new target value is not equal to current value
                                        targetValue = Random.nextInt(
                                            range = currentState.emergencyTaskConfig.valueRange
                                        )
                                    }

                                    currentState.emergencyTaskConfig.copy(
                                        targetValue = targetValue,
                                        currentValue = currentValue,
                                        remainingMatches = remainingMatches
                                    )
                                }
                        )
                    }
                }
            }
            else -> { /* no-op */ }
        }
    }
}