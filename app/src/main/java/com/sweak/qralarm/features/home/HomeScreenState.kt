package com.sweak.qralarm.features.home

import com.sweak.qralarm.features.home.components.model.AlarmWrapper

data class HomeScreenState(
    val isLoading: Boolean = true,
    val alarmWrappers: List<AlarmWrapper> = emptyList(),
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isOptimizationGuideDialogVisible: Boolean = false,
    val isAlarmMissedDialogVisible: Boolean = false,
    val deleteAlarmDialogState: DeleteAlarmDialogState = DeleteAlarmDialogState()
) {
    data class PermissionsDialogState(
        val isVisible: Boolean = false,
        val cameraPermissionState: Boolean? = null,
        val notificationsPermissionState: Boolean? = null,
        val alarmsPermissionState: Boolean? = null,
        val fullScreenIntentPermissionState: Boolean? = null
    )

    data class DeleteAlarmDialogState(
        val isVisible: Boolean = false,
        val alarmId: Long? = null
    )
}
