package com.sweak.qralarm.features.custom_code_scanner

sealed class CustomCodeScannerScreenUserEvent {
    data class CodeResultScanned(val codeResult: String) : CustomCodeScannerScreenUserEvent()
    data object OnCloseClicked : CustomCodeScannerScreenUserEvent()
}