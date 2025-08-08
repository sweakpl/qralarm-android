package com.sweak.qralarm.features.custom_code_scanner

import android.content.Context
import androidx.lifecycle.LifecycleOwner

sealed class CustomCodeScannerScreenUserEvent2 {
    data object CloseClicked : CustomCodeScannerScreenUserEvent2()
    data class BindToCamera(
        val appContext: Context,
        val lifecycleOwner: LifecycleOwner
    ) : CustomCodeScannerScreenUserEvent2()
    data object ToggleFlash : CustomCodeScannerScreenUserEvent2()
}