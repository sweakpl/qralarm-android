package com.sweak.qralarm.features.home.components.model

import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper

data class AlarmWrapper(
    val alarmId: Long,
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
    val isAlarmEnabled: Boolean,
    val isCodeEnabled: Boolean,
    val skipNextAlarmConfig: SkipNextAlarmConfig = SkipNextAlarmConfig()
) {
    data class SkipNextAlarmConfig(
        val isSkippingSupported: Boolean = false,
        val isSkippingNextAlarm: Boolean = false
    )
}
