package com.sweak.qralarm.features.emergency.task

sealed class EmergencyScreenUserEvent {
    data object OnCloseClicked : EmergencyScreenUserEvent()
    data object OnTaskStarted : EmergencyScreenUserEvent()
    data class OnTaskValueChanged(val value: Int) : EmergencyScreenUserEvent()
    data object OnTaskValueSelected : EmergencyScreenUserEvent()
    data class OnExplanationChanged(val text: String) : EmergencyScreenUserEvent()
    data object OnSubmitExplanation : EmergencyScreenUserEvent()
    data class OnScreenVisibilityChanged(val isVisible: Boolean) : EmergencyScreenUserEvent()
    data class OnRingAgainInMinutes(val minutes: Int) : EmergencyScreenUserEvent()
    data class OnRingAgainAt(val hourOfDay: Int, val minute: Int) : EmergencyScreenUserEvent()
    data object OnRingAgainDeclined : EmergencyScreenUserEvent()
}
