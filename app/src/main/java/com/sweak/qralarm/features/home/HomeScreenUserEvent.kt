package com.sweak.qralarm.features.home

sealed class HomeScreenUserEvent {
    data object MenuClicked : HomeScreenUserEvent()
    data object AddNewAlarm : HomeScreenUserEvent()
    data class EditAlarmClicked(val alarmId: Long) : HomeScreenUserEvent()
    data class AlarmEnabledChangeClicked(
        val alarmId: Long? = null,
        val enabled: Boolean? = null
    ) : HomeScreenUserEvent()
    data class TryChangeAlarmEnabled(
        val alarmId: Long? = null,
        val enabled: Boolean? = null,
        val cameraPermissionStatus: Boolean,
        val notificationsPermissionStatus: Boolean
    ) : HomeScreenUserEvent()
    data object HideMissingPermissionsDialog : HomeScreenUserEvent()
    data object RequestCameraPermission : HomeScreenUserEvent()
    data object RequestNotificationsPermission : HomeScreenUserEvent()
    data object RequestAlarmsPermission : HomeScreenUserEvent()
    data object RequestFullScreenIntentPermission : HomeScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : HomeScreenUserEvent()
    data class NotificationsPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : HomeScreenUserEvent()
    data object GoToApplicationSettingsClicked : HomeScreenUserEvent()
    data class OptimizationGuideDialogVisible(val isVisible: Boolean) : HomeScreenUserEvent()
    data object GoToOptimizationClicked : HomeScreenUserEvent()
    data class AlarmMissedDialogVisible(val isVisible: Boolean) : HomeScreenUserEvent()
    data class TryDeleteAlarm(val alarmId: Long) : HomeScreenUserEvent()
    data object HideDeleteAlarmDialog : HomeScreenUserEvent()
    data class DeleteAlarm(val alarmId: Long) : HomeScreenUserEvent()
    data class SkipNextAlarmChanged(val alarmId: Long, val skip: Boolean) : HomeScreenUserEvent()
}