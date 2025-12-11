package com.sweak.qralarm.app.activity

sealed class MainActivityUserEvent {
    data object ObserveActiveAlarms : MainActivityUserEvent()
    data object OnAlarmSaved : MainActivityUserEvent()
}