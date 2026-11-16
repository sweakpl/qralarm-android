package com.sweak.qralarm.features.alarm

import com.sweak.qralarm.core.ui.model.AlarmPermission

sealed class AlarmScreenUserEvent {
    data object StopAlarmClicked : AlarmScreenUserEvent()
    data class TryStopAlarm(val cameraPermissionStatus: Boolean) : AlarmScreenUserEvent()
    data object SnoozeAlarmClicked : AlarmScreenUserEvent()
    data object HideMissingPermissionsDialog : AlarmScreenUserEvent()
    data class RequestPermission(val permission: AlarmPermission) : AlarmScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(val isVisible: Boolean) : AlarmScreenUserEvent()
    data object GoToApplicationSettingsClicked : AlarmScreenUserEvent()
    data object UpdateCurrentTime : AlarmScreenUserEvent()
    data object OnEmergencyClicked : AlarmScreenUserEvent()
}