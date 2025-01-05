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
    val isOpenCodeLinkEnabled: Boolean,
    val isOneHourLockEnabled: Boolean,
    val alarmLabel: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val temporaryMuteDurationInSeconds: Int,
    val skipAlarmUntilTimeInMillis: Long?
) {
    sealed class RepeatingMode {
        data object Once : RepeatingMode()
        data class Days(val repeatingDaysOfWeek: List<DayOfWeek>) : RepeatingMode()
    }

    data class SnoozeConfig(
        val snoozeMode: SnoozeMode,
        val numberOfSnoozesLeft: Int,
        val isAlarmSnoozed: Boolean,
        val nextSnoozedAlarmTimeInMillis: Long?
    )

    data class SnoozeMode(
        val numberOfSnoozes: Int,
        val snoozeDurationInMinutes: Int
    )

    enum class Ringtone {
        GENTLE_GUITAR,
        KALIMBA,
        CLASSIC_ALARM,
        ALARM_CLOCK,
        ROOSTER,
        AIR_HORN,
        CUSTOM_SOUND
    }
}
