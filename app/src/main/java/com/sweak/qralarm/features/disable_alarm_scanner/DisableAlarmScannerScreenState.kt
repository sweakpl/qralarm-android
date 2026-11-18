package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.camera.core.SurfaceRequest
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectDetectionBox

data class DisableAlarmScannerScreenState(
    val surfaceRequest: SurfaceRequest? = null,
    val isFlashEnabled: Boolean = false,
    val codeName: String? = null,
    val shouldShowIncorrectCodeWarning: Boolean = false,
    val objectName: String? = null,
    val isCustomObject: Boolean = false,
    val customObjectsShown: Int = 0,
    val customObjectsTotal: Int = 0,
    val detectionBox: ObjectDetectionBox? = null,
    val confirmationProgress: Float = 0f,
    val recognitionError: Boolean = false
)
