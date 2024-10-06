package com.sweak.qralarm.features.scanner

sealed class ScannerScreenBackendEvent {
    data object CustomCodeSaved : ScannerScreenBackendEvent()
}