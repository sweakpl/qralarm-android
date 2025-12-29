package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.camera.core.SurfaceRequest

data class DisableAlarmScannerScreenState(
    val surfaceRequest: SurfaceRequest? = null,
    val isFlashEnabled: Boolean = false,
    val shouldShowIncorrectCodeWarning: Boolean = false
)
