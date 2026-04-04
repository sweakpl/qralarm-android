package com.sweak.qralarm.features.onboarding.permissions

sealed class PermissionsScreenUserEvent {
    data object CameraPermissionClicked : PermissionsScreenUserEvent()
    data object AlarmsPermissionClicked : PermissionsScreenUserEvent()
    data object NotificationsPermissionClicked : PermissionsScreenUserEvent()
    data object FullScreenIntentPermissionClicked : PermissionsScreenUserEvent()
    data object BackgroundWorkPermissionClicked : PermissionsScreenUserEvent()
    data object LetsGoClicked : PermissionsScreenUserEvent()
    data object GoToApplicationSettingsClicked : PermissionsScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : PermissionsScreenUserEvent()

    data class NotificationsPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : PermissionsScreenUserEvent()

    data class PermissionsUpdated(
        val cameraPermissionGranted: Boolean,
        val notificationsPermissionGranted: Boolean
    ) : PermissionsScreenUserEvent()
}
