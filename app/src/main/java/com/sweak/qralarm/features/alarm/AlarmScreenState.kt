package com.sweak.qralarm.features.alarm

data class AlarmScreenState(
    val currentTimeInMillis: Long = System.currentTimeMillis(),
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false
) {
    data class PermissionsDialogState(
        val isVisible: Boolean = false,
        val cameraPermissionState: Boolean? = null
    )
}