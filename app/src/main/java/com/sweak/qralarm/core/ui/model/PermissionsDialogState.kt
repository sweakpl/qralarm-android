package com.sweak.qralarm.core.ui.model

data class PermissionsDialogState(
    val isVisible: Boolean = false,
    val cameraPermissionState: Boolean? = null,
    val notificationsPermissionState: Boolean? = null,
    val alarmsPermissionState: Boolean? = null,
    val fullScreenIntentPermissionState: Boolean? = null
) {
    val areAllPermissionsGranted: Boolean
        get() = cameraPermissionState != false &&
                notificationsPermissionState != false &&
                alarmsPermissionState != false &&
                fullScreenIntentPermissionState != false
}
