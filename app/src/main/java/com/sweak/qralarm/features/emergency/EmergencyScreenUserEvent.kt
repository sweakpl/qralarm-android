package com.sweak.qralarm.features.emergency

sealed class EmergencyScreenUserEvent {
    data object OnCloseClicked : EmergencyScreenUserEvent()
    data object OnTaskStarted : EmergencyScreenUserEvent()
    data class OnTaskValueChanged(val value: Int) : EmergencyScreenUserEvent()
    data object OnTaskValueSelected : EmergencyScreenUserEvent()
}