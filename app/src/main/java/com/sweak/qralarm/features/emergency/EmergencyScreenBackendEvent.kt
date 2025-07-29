package com.sweak.qralarm.features.emergency

sealed class EmergencyScreenBackendEvent {
    data object EmergencyTaskCompleted : EmergencyScreenBackendEvent()
}