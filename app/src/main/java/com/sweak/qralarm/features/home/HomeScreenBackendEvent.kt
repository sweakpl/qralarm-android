package com.sweak.qralarm.features.home

sealed class HomeScreenBackendEvent {
    data class AlarmSet(
        val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>
    ): HomeScreenBackendEvent()
    data class RedirectToEditAlarm(val alarmId: Long) : HomeScreenBackendEvent()
    data object CanNotEditAlarm : HomeScreenBackendEvent()
    data class CanNotDisableAlarm(val alarmId: Long) : HomeScreenBackendEvent()
}