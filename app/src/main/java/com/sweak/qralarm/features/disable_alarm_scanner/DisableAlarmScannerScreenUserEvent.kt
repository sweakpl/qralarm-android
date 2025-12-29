package com.sweak.qralarm.features.disable_alarm_scanner

import android.content.Context
import androidx.compose.ui.platform.WindowInfo
import androidx.lifecycle.LifecycleOwner

sealed class DisableAlarmScannerScreenUserEvent {
    data class InitializeCamera(
        val appContext: Context,
        val lifecycleOwner: LifecycleOwner,
        val windowInfo: WindowInfo
    ) : DisableAlarmScannerScreenUserEvent()
    data object OnCloseClicked : DisableAlarmScannerScreenUserEvent()
    data object ToggleFlash : DisableAlarmScannerScreenUserEvent()
}