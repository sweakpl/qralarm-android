package com.sweak.qralarm.features.home

import androidx.compose.material3.SnackbarHostState
import com.sweak.qralarm.features.home.components.model.AlarmWrapper

data class HomeScreenState(
    val isLoading: Boolean = true,
    val activeAlarmWrappers: List<AlarmWrapper> = emptyList(),
    val nonActiveAlarmWrappers: List<AlarmWrapper> = emptyList(),
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isOptimizationGuideDialogVisible: Boolean = false,
    val isAlarmMissedDialogVisible: Boolean = false,
    val deleteAlarmDialogState: DeleteAlarmDialogState = DeleteAlarmDialogState(),
    val upcomingAlarmMessages: List<UpcomingAlarmMessage> = emptyList(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
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

    data class UpcomingAlarmMessage(
        val alarmId: Long,
        val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>
    )
}
