package com.sweak.qralarm.features.home

sealed class HomeScreenBackendEvent {
    data class RedirectToAddEditAlarm(val alarmId: Long? = null) : HomeScreenBackendEvent()
    data object CanNotEditAlarm : HomeScreenBackendEvent()
    data class CanNotDisableAlarm(val alarmId: Long) : HomeScreenBackendEvent()
}