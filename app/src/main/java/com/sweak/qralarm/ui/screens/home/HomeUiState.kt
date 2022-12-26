package com.sweak.qralarm.ui.screens.home
import androidx.compose.material.SnackbarHostState

data class HomeUiState(
    var alarmSet: Boolean,
    var alarmServiceRunning: Boolean,
    var snoozeAvailable: Boolean,
    var showAlarmPermissionDialog: Boolean,
    var showCameraPermissionDialog: Boolean,
    var showCameraPermissionRevokedDialog: Boolean,
    var showNotificationsPermissionDialog: Boolean,
    var showNotificationsPermissionRevokedDialog: Boolean,
    var snackbarHostState: SnackbarHostState,
    var minutesSpeed: Float
)