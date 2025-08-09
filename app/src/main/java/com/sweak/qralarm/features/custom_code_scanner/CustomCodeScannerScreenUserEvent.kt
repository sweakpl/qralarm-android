package com.sweak.qralarm.features.custom_code_scanner

import android.content.Context
import androidx.compose.ui.platform.WindowInfo
import androidx.lifecycle.LifecycleOwner

sealed class CustomCodeScannerScreenUserEvent {
    data object CloseClicked : CustomCodeScannerScreenUserEvent()
    data class InitializeCamera(
        val appContext: Context,
        val lifecycleOwner: LifecycleOwner,
        val windowInfo: WindowInfo
    ) : CustomCodeScannerScreenUserEvent()
    data object ToggleFlash : CustomCodeScannerScreenUserEvent()
}