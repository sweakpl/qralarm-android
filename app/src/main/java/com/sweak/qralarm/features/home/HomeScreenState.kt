package com.sweak.qralarm.features.home

import com.sweak.qralarm.features.home.components.model.AlarmWrapper

data class HomeScreenState(
    val alarmWrappers: List<AlarmWrapper> = emptyList()
)
