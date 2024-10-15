package com.sweak.qralarm.features.home

sealed class HomeScreenBackendEvent {
    data class AlarmSet(
        val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>
    ): HomeScreenBackendEvent()
}