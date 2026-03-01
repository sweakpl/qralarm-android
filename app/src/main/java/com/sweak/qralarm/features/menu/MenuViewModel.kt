package com.sweak.qralarm.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val alarmsRepository: AlarmsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MenuScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val allSavedAlarmCodes = alarmsRepository.getAllAlarms()
                .map { alarms ->
                    alarms
                        .mapNotNull { alarm -> alarm.assignedCode }
                        .distinct()
                }
                .first()

            _state.update { currentState ->
                currentState.copy(
                    defaultAlarmCode = userDataRepository.defaultAlarmCode.first(),
                    previouslySavedCodes = allSavedAlarmCodes
                )
            }
        }

        viewModelScope.launch {
            userDataRepository.defaultAlarmCode.collect { defaultAlarmCode ->
                _state.update { currentState ->
                    currentState.copy(defaultAlarmCode = defaultAlarmCode)
                }
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

    fun onEvent(event: MenuScreenUserEvent) {
        when (event) {
            is MenuScreenUserEvent.AssignDefaultCodeDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = event.isVisible)
                }
            }
            is MenuScreenUserEvent.DefaultCodeChosenFromList -> viewModelScope.launch {
                userDataRepository.setDefaultAlarmCode(code = event.code)

                _state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = false)
                }
            }
            is MenuScreenUserEvent.ClearDefaultAlarmCode -> viewModelScope.launch {
                userDataRepository.setDefaultAlarmCode(code = null)

                _state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = false)
                }
            }
            is MenuScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            else -> { /* no-op */ }
        }
    }
}