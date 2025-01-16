package com.sweak.qralarm.features.disable_alarm_scanner

import com.google.zxing.Result

sealed class DisableAlarmScannerScreenUserEvent {
    data class CodeResultScanned(val result: Result) : DisableAlarmScannerScreenUserEvent()
    data object OnCloseClicked : DisableAlarmScannerScreenUserEvent()
}