package com.sweak.qralarm.features.alarm

sealed class AlarmScreenUserEvent {
    data object StopAlarmClicked : AlarmScreenUserEvent()
    data class TryStopAlarm(val cameraPermissionStatus: Boolean) : AlarmScreenUserEvent()
    data object SnoozeAlarmClicked : AlarmScreenUserEvent()
    data object HideMissingPermissionsDialog : AlarmScreenUserEvent()
    data object RequestCameraPermission : AlarmScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(val isVisible: Boolean) : AlarmScreenUserEvent()
    data object GoToApplicationSettingsClicked : AlarmScreenUserEvent()
    data object UpdateCurrentTime : AlarmScreenUserEvent()
}