package com.sweak.qralarm.core.data.backup

import com.sweak.qralarm.core.domain.backup.BackupAlarm
import com.sweak.qralarm.core.domain.backup.BackupCode
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import com.sweak.qralarm.core.storage.database.model.CodeEntity

fun AlarmEntity.toBackupAlarm(): BackupAlarm = BackupAlarm(
    alarmId = alarmId,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    repeatingAlarmDays = repeatingAlarmDays,
    numberOfSnoozes = numberOfSnoozes,
    snoozeDurationInMinutes = snoozeDurationInMinutes,
    ringtone = ringtone,
    // Narrowed down further by CreateBackup, which also checks that the file is actually there.
    hasCustomRingtoneFile = customRingtoneUriString != null,
    alarmVolumePercentage = alarmVolumePercentage,
    areVibrationsEnabled = areVibrationsEnabled,
    isUsingCode = isUsingCode,
    assignedCodeId = assignedCodeId,
    isOpenCodeLinkEnabled = isOpenCodeLinkEnabled,
    cancelLockDurationInMinutes = cancelLockDurationInMinutes,
    isEmergencyTaskEnabled = isEmergencyTaskEnabled,
    alarmLabel = alarmLabel,
    gentleWakeUpDurationInSeconds = gentleWakeUpDurationInSeconds,
    temporaryMuteDurationInSeconds = temporaryMuteDurationInSeconds
)

fun CodeEntity.toBackupCode(): BackupCode = BackupCode(
    codeId = codeId,
    value = value,
    name = name
)

/*
 * The two functions below build entities with the full named constructor and never with copy().
 * That is deliberate: AlarmEntity gives a Kotlin default to alarmId only - the
 * @ColumnInfo(defaultValue = ...) annotations are SQL defaults, not Kotlin ones - so adding a
 * column breaks the build right here and forces an export-or-exclude decision on it.
 */

fun BackupCode.toCodeEntity(): CodeEntity = CodeEntity(
    codeId = 0,
    value = value,
    name = name
)

fun BackupAlarm.toAlarmEntity(assignedCodeId: Long?): AlarmEntity = AlarmEntity(
    alarmId = 0,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    isAlarmEnabled = false,
    isAlarmRunning = false,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    repeatingAlarmDays = repeatingAlarmDays,
    numberOfSnoozes = numberOfSnoozes,
    snoozeDurationInMinutes = snoozeDurationInMinutes,
    numberOfSnoozesLeft = numberOfSnoozes,
    isAlarmSnoozed = false,
    nextSnoozedAlarmTimeInMillis = null,
    ringtone = ringtone,
    // Rebuilt by RestoreBackup once the file has been unpacked to its new location.
    customRingtoneUriString = null,
    alarmVolumePercentage = alarmVolumePercentage,
    areVibrationsEnabled = areVibrationsEnabled,
    isUsingCode = isUsingCode,
    assignedCodeId = assignedCodeId,
    isOpenCodeLinkEnabled = isOpenCodeLinkEnabled,
    cancelLockDurationInMinutes = cancelLockDurationInMinutes,
    isEmergencyTaskEnabled = isEmergencyTaskEnabled,
    alarmLabel = alarmLabel,
    gentleWakeUpDurationInSeconds = gentleWakeUpDurationInSeconds,
    temporaryMuteDurationInSeconds = temporaryMuteDurationInSeconds,
    skipAlarmUntilTimeInMillis = null
)
