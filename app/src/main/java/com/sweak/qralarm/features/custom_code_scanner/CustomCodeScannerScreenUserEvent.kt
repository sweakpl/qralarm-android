package com.sweak.qralarm.features.custom_code_scanner

import com.google.zxing.Result

sealed class CustomCodeScannerScreenUserEvent {
    data class CodeResultScanned(val result: Result) : CustomCodeScannerScreenUserEvent()
    data object OnCloseClicked : CustomCodeScannerScreenUserEvent()
}