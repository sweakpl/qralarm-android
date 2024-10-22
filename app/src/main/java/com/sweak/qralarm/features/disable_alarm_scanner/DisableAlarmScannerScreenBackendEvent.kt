package com.sweak.qralarm.features.disable_alarm_scanner

sealed class DisableAlarmScannerScreenBackendEvent {
    data object CorrectCodeScanned : DisableAlarmScannerScreenBackendEvent()
}