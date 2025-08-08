package com.sweak.qralarm.features.custom_code_scanner

sealed class CustomCodeScannerScreenBackendEvent2 {
    data object CustomCodeSaved : CustomCodeScannerScreenBackendEvent2()
}