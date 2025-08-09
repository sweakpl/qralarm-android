package com.sweak.qralarm.features.custom_code_scanner

import android.content.Context
import androidx.compose.ui.platform.WindowInfo
import androidx.lifecycle.LifecycleOwner

sealed class CustomCodeScannerScreenUserEvent2 {
    data object CloseClicked : CustomCodeScannerScreenUserEvent2()
    data class InitializeCamera(
        val appContext: Context,
        val lifecycleOwner: LifecycleOwner,
        val windowInfo: WindowInfo
    ) : CustomCodeScannerScreenUserEvent2()
    data object ToggleFlash : CustomCodeScannerScreenUserEvent2()
}