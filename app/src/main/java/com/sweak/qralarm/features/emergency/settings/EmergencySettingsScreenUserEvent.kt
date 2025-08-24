package com.sweak.qralarm.features.emergency.settings

sealed class EmergencySettingsScreenUserEvent {
    data object BackClicked : EmergencySettingsScreenUserEvent()
    data class SliderRangeSelected(val index: Int) : EmergencySettingsScreenUserEvent()
    data class RequiredMatchesSelected(val index: Int) : EmergencySettingsScreenUserEvent()
    data object PreviewEmergencyTaskClicked : EmergencySettingsScreenUserEvent()
}