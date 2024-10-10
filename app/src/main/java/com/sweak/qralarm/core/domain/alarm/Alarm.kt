package com.sweak.qralarm.core.domain.alarm

import java.time.DayOfWeek

data class Alarm(
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val isAlarmEnabled: Boolean,
    val repeatingMode: RepeatingMode,
    val snoozeMode: SnoozeMode,
    val ringtone: Ringtone,
    val customRingtoneUriString: String?,
    val areVibrationsEnabled: Boolean,
    val isUsingCode: Boolean,
    val assignedCode: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val isTemporaryMuteEnabled: Boolean
) {
    sealed class RepeatingMode {
        data class Once(val alarmDayInMillis: Long) : RepeatingMode()
        data class Days(val repeatingDaysOfWeek: List<DayOfWeek>) : RepeatingMode()
    }

    data class SnoozeMode(
        val numberOfSnoozes: Int,
        val snoozeDurationInMinutes: Int
    )

    enum class Ringtone {
        GENTLE_GUITAR,
        ALARM_CLOCK,
        AIR_HORN,
        CUSTOM_SOUND
    }
}
