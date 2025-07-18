package com.sweak.qralarm.features.alarm.destinations.emergency

sealed class EmergencyScreenBackendEvent {
    data object EmergencyTaskCompleted : EmergencyScreenBackendEvent()
}