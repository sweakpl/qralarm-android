package com.sweak.qralarm.features.menu

sealed class MenuScreenUserEvent {
    data object OnBackClicked : MenuScreenUserEvent()
    data object OnOptimizationGuideClicked : MenuScreenUserEvent()
    data object OnEmergencyTaskSettingsClicked : MenuScreenUserEvent()
    data object OnQRAlarmProClicked : MenuScreenUserEvent()
    data object OnRateQRAlarmClicked : MenuScreenUserEvent()
    data object OnCodesManagementClicked : MenuScreenUserEvent()
    data object GoToApplicationSettingsClicked : MenuScreenUserEvent()
    data object OnThemeClicked : MenuScreenUserEvent()
}
