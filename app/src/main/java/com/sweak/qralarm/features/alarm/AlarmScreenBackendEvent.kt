package com.sweak.qralarm.features.alarm

sealed class AlarmScreenBackendEvent {
    data object StopAlarm : AlarmScreenBackendEvent()
    data object RequestCodeScanToStopAlarm : AlarmScreenBackendEvent()
    data object SnoozeAlarm : AlarmScreenBackendEvent()
    data class TryTemporarilyMuteAlarm(val muteDurationInSeconds: Int) : AlarmScreenBackendEvent()
}