package com.sweak.qralarm.features.home

sealed class HomeScreenUserEvent {
    data object AddNewAlarm : HomeScreenUserEvent()
    data class EditAlarm(val alarmId: Long) : HomeScreenUserEvent()
    data class AlarmEnabledChanged(val alarmId: Long, val enabled: Boolean) : HomeScreenUserEvent()
}