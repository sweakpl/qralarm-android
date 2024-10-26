package com.sweak.qralarm.ui.screens.home

import com.sweak.qralarm.util.TimeFormat

data class HomeUiState(
    var alarmTimeFormat: TimeFormat,
    var alarmHourOfDay: Int,
    var alarmMinute: Int,
    var alarmSet: Boolean,
    var alarmServiceRunning: Boolean,
    var snoozeAvailable: Boolean,
    var isManualAlarmScheduling: Boolean,
    var showAlarmPermissionDialog: Boolean,
    var showCameraPermissionDialog: Boolean,
    var showCameraPermissionRevokedDialog: Boolean,
    var showNotificationsPermissionDialog: Boolean,
    var showNotificationsPermissionRevokedDialog: Boolean,
    var showCodePossessionConfirmationDialog: Boolean,
    var showFullScreenIntentPermissionDialog: Boolean,
    var showEnableRepeatingAlarmsDialog: Boolean,
    var showDisableRepeatingAlarmsDialog: Boolean
)