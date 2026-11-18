package com.sweak.qralarm.features.emergency.settings

import com.sweak.qralarm.core.domain.user.model.EmergencyTaskType

sealed class EmergencySettingsScreenUserEvent {
    data object BackClicked : EmergencySettingsScreenUserEvent()
    data class SliderRangeSelected(val index: Int) : EmergencySettingsScreenUserEvent()
    data class RequiredMatchesSelected(val index: Int) : EmergencySettingsScreenUserEvent()
    data class DisableRepeatingAlarmsEnabledChanged(
        val isEnabled: Boolean
    ) : EmergencySettingsScreenUserEvent()
    data object PreviewEmergencyTaskClicked : EmergencySettingsScreenUserEvent()
    data class TaskTypeSelected(val type: EmergencyTaskType) : EmergencySettingsScreenUserEvent()
    data object ExplanationHistoryClicked : EmergencySettingsScreenUserEvent()
}