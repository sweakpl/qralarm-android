package com.sweak.qralarm.features.theme

sealed class ThemeScreenBackendEvent {
    data object RedirectToQRAlarmPro : ThemeScreenBackendEvent()
}
