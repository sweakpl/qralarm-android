package com.sweak.qralarm.core.domain.backup

import com.sweak.qralarm.core.domain.user.model.Theme

/**
 * Everything a backup carries over: the alarms, the saved codes and the handful of settings worth
 * keeping. Ids are the ones the alarms and codes had when the backup was made - restoring hands
 * out fresh ones and remaps every reference to them.
 */
data class BackupBundle(
    val alarms: List<BackupAlarm>,
    val codes: List<BackupCode>,
    val preferences: BackupPreferences
)

/**
 * Says where a bundle came from. Only [backupFormatVersion] is ever acted upon - a bundle written
 * in a newer format than this version of the app understands cannot be restored. Everything else
 * is there to be looked at, never to be gated on.
 */
data class BackupMetadata(
    val backupFormatVersion: Int,
    val createdAtEpochMillis: Long,
    val applicationId: String,
    val flavor: String,
    val versionCode: Int,
    val versionName: String,
    val databaseSchemaVersion: Int
)

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

/**
 * The settings a backup carries. A null value means the bundle says nothing about that setting
 * and whatever is currently set is left alone - except [defaultAlarmCodeId], for
 * which null also legitimately means "no default code" and restores to exactly that.
 */
data class BackupPreferences(
    /** The id the code had when the backup was made. */
    val defaultAlarmCodeId: Long?,
    val emergencySliderRange: IntRange?,
    val emergencyRequiredMatches: Int?,
    val emergencyDisableRepeatingAlarms: Boolean?,
    val theme: Theme?
)
