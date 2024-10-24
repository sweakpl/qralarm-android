package com.sweak.qralarm.core.domain.alarm

import java.time.DayOfWeek

data class Alarm(
    val alarmId: Long,
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val isAlarmEnabled: Boolean,
    val isAlarmRunning: Boolean,
    val repeatingMode: RepeatingMode,
    val nextAlarmTimeInMillis: Long,
    val snoozeConfig: SnoozeConfig,
    val ringtone: Ringtone,
    val customRingtoneUriString: String?,
    val areVibrationsEnabled: Boolean,
    val isUsingCode: Boolean,
    val assignedCode: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val isTemporaryMuteEnabled: Boolean
) {
    sealed class RepeatingMode {
        data object Once : RepeatingMode()
        data class Days(val repeatingDaysOfWeek: List<DayOfWeek>) : RepeatingMode()
    }

    data class SnoozeConfig(
        val snoozeMode: SnoozeMode,
        val numberOfSnoozesLeft: Int,
        val isAlarmSnoozed: Boolean
    )

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
