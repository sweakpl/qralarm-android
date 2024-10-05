package com.sweak.qralarm.features.home

sealed class HomeScreenUserEvent {
    data object AddNewAlarm : HomeScreenUserEvent()
}