package com.sweak.qralarm.features.scanner

import com.google.zxing.Result

sealed class ScannerScreenUserEvent {
    data class CodeResultScanned(val result: Result) : ScannerScreenUserEvent()
}