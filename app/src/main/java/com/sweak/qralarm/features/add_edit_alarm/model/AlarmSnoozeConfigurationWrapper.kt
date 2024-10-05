package com.sweak.qralarm.features.add_edit_alarm.model

data class AlarmSnoozeConfigurationWrapper(
    val numberOfSnoozes: Int = 3,
    val snoozeDurationInMinutes: Int = 10
)
