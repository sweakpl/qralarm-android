package com.sweak.qralarm.features.emergency.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_REQUIRED_MATCHES
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_SLIDER_RANGES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencySettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private var _state = MutableStateFlow(EmergencySettingsScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    availableSliderRanges = EMERGENCY_AVAILABLE_SLIDER_RANGES,
                    selectedSliderRangeIndex = EMERGENCY_AVAILABLE_SLIDER_RANGES.indexOf(
                        userDataRepository.emergencySliderRange.first()
                    ),
                    availableRequiredMatches = EMERGENCY_AVAILABLE_REQUIRED_MATCHES,
                    selectedRequiredMatchesIndex = EMERGENCY_AVAILABLE_REQUIRED_MATCHES.indexOf(
                        userDataRepository.emergencyRequiredMatches.first()
                    )
                )
            }
        }
    }

    fun onEvent(event: EmergencySettingsScreenUserEvent) {
        when (event) {
            is EmergencySettingsScreenUserEvent.SliderRangeSelected -> viewModelScope.launch {
                val sliderRange = EMERGENCY_AVAILABLE_SLIDER_RANGES[event.index]

                userDataRepository.setEmergencySliderRange(range = sliderRange)

                _state.update { currentState ->
                    currentState.copy(selectedSliderRangeIndex = event.index)
                }
            }
            is EmergencySettingsScreenUserEvent.RequiredMatchesSelected -> viewModelScope.launch {
                val requiredMatches = EMERGENCY_AVAILABLE_REQUIRED_MATCHES[event.index]

                userDataRepository.setEmergencyRequiredMatches(matches = requiredMatches)

                _state.update { currentState ->
                    currentState.copy(selectedRequiredMatchesIndex = event.index)
                }
            }
            else -> { /* no-op */ }
        }
    }
}