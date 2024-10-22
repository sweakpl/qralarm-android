package com.sweak.qralarm.features.custom_code_scanner

sealed class CustomCodeScannerScreenBackendEvent {
    data object CustomCodeSaved : CustomCodeScannerScreenBackendEvent()
}