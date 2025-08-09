package com.sweak.qralarm.features.custom_code_scanner

import androidx.camera.core.SurfaceRequest

data class CustomCodeScannerScreenState(
    val surfaceRequest: SurfaceRequest? = null,
    val isFlashEnabled: Boolean = false
)
