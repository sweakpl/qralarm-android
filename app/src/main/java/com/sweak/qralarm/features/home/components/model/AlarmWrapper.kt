package com.sweak.qralarm.features.home.components.model

import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper

data class AlarmWrapper(
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
    val isAlarmEnabled: Boolean,
    val isQRCOdeEnabled: Boolean
)
