package com.sweak.qralarm.features.alarm

import com.sweak.qralarm.core.ui.compose_util.UiText

data class AlarmScreenState(
    val currentTimeInMillis: Long = System.currentTimeMillis(),
    val alarmLabel: UiText? = null,
    val timeToShow: Long = System.currentTimeMillis(),
    val isSnoozeAvailable: Boolean = false,
    val isInteractionEnabled: Boolean = true,
    val isEmergencyAvailable: Boolean = false,
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false
) {
    data class PermissionsDialogState(
        val isVisible: Boolean = false,
        val cameraPermissionState: Boolean? = null
    )
}