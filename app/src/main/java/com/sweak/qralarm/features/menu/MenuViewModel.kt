package com.sweak.qralarm.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    val state = MutableStateFlow(MenuScreenState())

    init {
        viewModelScope.launch {
            val allSavedAlarmCodes = alarmsRepository.getAllAlarms()
                .map { alarms ->
                    alarms
                        .mapNotNull { alarm -> alarm.assignedCode }
                        .distinct()
                }
                .first()

            state.update { currentState ->
                currentState.copy(
                    defaultAlarmCode = userDataRepository.defaultAlarmCode.first(),
                    previouslySavedCodes = allSavedAlarmCodes
                )
            }
        }

        viewModelScope.launch {
            userDataRepository.defaultAlarmCode.collect { defaultAlarmCode ->
                state.update { currentState ->
                    currentState.copy(defaultAlarmCode = defaultAlarmCode)
                }
            }
        }
    }

    fun onEvent(event: MenuScreenUserEvent) {
        when (event) {
            is MenuScreenUserEvent.AssignDefaultCodeDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = event.isVisible)
                }
            }
            is MenuScreenUserEvent.DefaultCodeChosenFromList -> viewModelScope.launch {
                userDataRepository.setDefaultAlarmCode(code = event.code)

                state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = false)
                }
            }
            is MenuScreenUserEvent.ClearDefaultAlarmCode -> viewModelScope.launch {
                userDataRepository.setDefaultAlarmCode(code = null)

                state.update { currentState ->
                    currentState.copy(isAssignDefaultCodeDialogVisible = false)
                }
            }
            is MenuScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                state.update { currentState ->
                    currentState.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }
            else -> { /* no-op */ }
        }
    }
}