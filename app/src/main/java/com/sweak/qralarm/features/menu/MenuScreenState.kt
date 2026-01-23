package com.sweak.qralarm.features.menu

import com.sweak.qralarm.core.domain.user.model.Theme

data class MenuScreenState(
    val defaultAlarmCode: String? = null,
    val isAssignDefaultCodeDialogVisible: Boolean = false,
    val previouslySavedCodes: List<String> = emptyList(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val theme: Theme = Theme.Default
)
