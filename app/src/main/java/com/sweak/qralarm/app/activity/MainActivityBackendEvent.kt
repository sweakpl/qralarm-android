package com.sweak.qralarm.app.activity

sealed class MainActivityBackendEvent {
    data class NavigateToActiveAlarm(val alarmId: Long) : MainActivityBackendEvent()
}