package com.sweak.qralarm.features.onboarding.permissions

sealed class PermissionsPageUserEvent {
    data object CameraPermissionClicked : PermissionsPageUserEvent()
    data object AlarmsPermissionClicked : PermissionsPageUserEvent()
    data object NotificationsPermissionClicked : PermissionsPageUserEvent()
    data object FullScreenIntentPermissionClicked : PermissionsPageUserEvent()
    data object BackgroundWorkPermissionClicked : PermissionsPageUserEvent()
    data object GoToApplicationSettingsClicked : PermissionsPageUserEvent()
    data class CameraPermissionDeniedDialogVisible(val isVisible: Boolean) : PermissionsPageUserEvent()
    data class NotificationsPermissionDeniedDialogVisible(val isVisible: Boolean) : PermissionsPageUserEvent()
    data class PermissionsUpdated(
        val cameraPermissionGranted: Boolean,
        val notificationsPermissionGranted: Boolean
    ) : PermissionsPageUserEvent()
    data object LetsGoClicked : PermissionsPageUserEvent()
}
