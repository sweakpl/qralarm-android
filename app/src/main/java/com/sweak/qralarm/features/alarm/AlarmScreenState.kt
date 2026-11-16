package com.sweak.qralarm.features.alarm

import com.sweak.qralarm.core.ui.compose_util.UiText
import com.sweak.qralarm.core.ui.model.PermissionsDialogState

data class AlarmScreenState(
    val alarmLabel: UiText? = null,
    val codeName: String? = null,
    val timeToShow: Long = System.currentTimeMillis(),
    val isAlarmSnoozed: Boolean = false,
    val isSnoozeAvailable: Boolean = false,
    val isInteractionEnabled: Boolean = true,
    val isEmergencyAvailable: Boolean = false,
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false
)