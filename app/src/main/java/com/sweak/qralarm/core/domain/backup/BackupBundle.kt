package com.sweak.qralarm.core.domain.backup

/**
 * A single alarm as it travels through a backup. Deliberately not a domain
 * [com.sweak.qralarm.core.domain.alarm.Alarm]: every value is carried exactly as it was saved and
 * is never interpreted on the way through, so that a backup round trip cannot alter it. That is
 * why [ringtone] and [repeatingAlarmDays] are plain strings rather than the richer types the rest
 * of the app works with.
 *
 * An alarm's runtime state is absent by design - restoring an alarm resets it, leaving it
 * disabled, not running, not snoozed, with its snoozes replenished and with nothing skipped.
 */
data class BackupAlarm(
    val alarmId: Long,
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val nextAlarmTimeInMillis: Long,
    /** Null means the alarm does not repeat. */
    val repeatingAlarmDays: String?,
    val numberOfSnoozes: Int,
    val snoozeDurationInMinutes: Int,
    /** The name of the alarm's [com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone]. */
    val ringtone: String,
    val hasCustomRingtoneFile: Boolean,
    val alarmVolumePercentage: Int,
    val areVibrationsEnabled: Boolean,
    val isUsingCode: Boolean,
    val assignedCodeId: Long?,
    val isOpenCodeLinkEnabled: Boolean,
    val cancelLockDurationInMinutes: Int,
    val isEmergencyTaskEnabled: Boolean,
    val alarmLabel: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val temporaryMuteDurationInSeconds: Int
)

data class BackupCode(
    val codeId: Long,
    val value: String,
    val name: String?
)
