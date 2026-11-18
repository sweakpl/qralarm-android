package com.sweak.qralarm.features.emergency.settings

import com.sweak.qralarm.core.domain.user.model.EmergencyTaskType

data class EmergencySettingsScreenState(
    val availableSliderRanges: List<IntRange> = emptyList(),
    val selectedSliderRangeIndex: Int? = null,
    val availableRequiredMatches: List<Int> = emptyList(),
    val selectedRequiredMatchesIndex: Int? = null,
    val isDisableRepeatingAlarmsEnabled: Boolean? = null,
    val taskType: EmergencyTaskType? = null
)
