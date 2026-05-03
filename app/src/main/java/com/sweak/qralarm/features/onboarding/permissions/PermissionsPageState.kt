package com.sweak.qralarm.features.onboarding.permissions

import com.sweak.qralarm.features.onboarding.permissions.util.PermissionsPagePermissionKey

data class PermissionsPageState(
    val cameraPermissionGranted: Boolean = false,
    val alarmsPermissionVisible: Boolean = false,
    val alarmsPermissionGranted: Boolean = true,
    val notificationsPermissionVisible: Boolean = false,
    val notificationsPermissionGranted: Boolean = true,
    val fullScreenIntentPermissionVisible: Boolean = false,
    val fullScreenIntentPermissionGranted: Boolean = true,
    val backgroundWorkPermissionGranted: Boolean = false,
    val permissionsRequiringInteraction: Set<PermissionsPagePermissionKey> = emptySet(),
    val interactedPermissions: Set<PermissionsPagePermissionKey> = emptySet(),
    val areAllRequiredPermissionsHandled: Boolean = false,
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false
)
