package com.sweak.qralarm.ui.screens.scanner

data class ScannerUiState(
    val showDismissCodeAddedDialog: Boolean = false,
    val newDismissAlarmCode: String? = null,
    val hasNewDismissCodeBeenAccepted: Boolean = false
)
