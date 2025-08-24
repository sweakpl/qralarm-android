package com.sweak.qralarm.features.emergency.settings

data class EmergencySettingsScreenState(
    val availableSliderRanges: List<IntRange> = emptyList(),
    val selectedSliderRangeIndex: Int? = null,
    val availableRequiredMatches: List<Int> = emptyList(),
    val selectedRequiredMatchesIndex: Int? = null
)
