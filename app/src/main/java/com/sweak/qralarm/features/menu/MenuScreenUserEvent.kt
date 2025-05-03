package com.sweak.qralarm.features.menu

sealed class MenuScreenUserEvent {
    data object OnBackClicked : MenuScreenUserEvent()
    data object OnIntroductionClicked : MenuScreenUserEvent()
    data object OnOptimizationGuideClicked : MenuScreenUserEvent()
    data object OnQRAlarmProClicked : MenuScreenUserEvent()
    data object OnRateQRAlarmClicked : MenuScreenUserEvent()
    data class AssignDefaultCodeDialogVisible(val isVisible: Boolean) : MenuScreenUserEvent()
    data object TryScanSpecificDefaultCode : MenuScreenUserEvent()
    data class DefaultCodeChosenFromList(val code: String) : MenuScreenUserEvent()
    data object ClearDefaultAlarmCode : MenuScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(val isVisible: Boolean) : MenuScreenUserEvent()
    data object GoToApplicationSettingsClicked : MenuScreenUserEvent()
}