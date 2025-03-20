package com.sweak.qralarm.features.disable_alarm_scanner

sealed class DisableAlarmScannerScreenUserEvent {
    data class CodeResultScanned(val codeResult: String) : DisableAlarmScannerScreenUserEvent()
    data object OnCloseClicked : DisableAlarmScannerScreenUserEvent()
}