package com.sweak.qralarm.features.onboarding.permissions

data class PermissionsScreenState(
    val cameraPermissionGranted: Boolean = false,
    val alarmsPermissionVisible: Boolean = false,
    val alarmsPermissionGranted: Boolean = true,
    val notificationsPermissionVisible: Boolean = false,
    val notificationsPermissionGranted: Boolean = true,
    val fullScreenIntentPermissionVisible: Boolean = false,
    val fullScreenIntentPermissionGranted: Boolean = true,
    val backgroundWorkPermissionGranted: Boolean = false,
    val permissionsRequiringInteraction: Set<String> = emptySet(),
    val interactedPermissions: Set<String> = emptySet(),
    val isLetsGoButtonEnabled: Boolean = false,
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
)
