package com.sweak.qralarm.features.emergency.task

sealed class EmergencyScreenBackendEvent {
    data object EmergencyTaskCompleted : EmergencyScreenBackendEvent()
}