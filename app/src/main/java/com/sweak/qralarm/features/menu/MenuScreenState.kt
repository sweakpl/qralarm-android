package com.sweak.qralarm.features.menu

data class MenuScreenState(
    val defaultAlarmCode: String? = null,
    val isAssignDefaultCodeDialogVisible: Boolean = false,
    val previouslySavedCodes: List<String> = emptyList(),
)
