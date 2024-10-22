package com.sweak.qralarm.features.alarm

sealed class AlarmScreenUserEvent {
    data object StopAlarmClicked : AlarmScreenUserEvent()
}