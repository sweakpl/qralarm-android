package com.sweak.qralarm.core.domain.fixture

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.backup.BackupAlarm
import com.sweak.qralarm.core.domain.backup.BackupBundle
import com.sweak.qralarm.core.domain.backup.BackupCode
import com.sweak.qralarm.core.domain.backup.BackupMetadata
import com.sweak.qralarm.core.domain.backup.BackupPreferences
import com.sweak.qralarm.core.domain.backup.CURRENT_BACKUP_FORMAT_VERSION
import com.sweak.qralarm.core.domain.user.model.Theme

fun backupAlarm(
    alarmId: Long,
    hasCustomRingtoneFile: Boolean = false
) = BackupAlarm(
    alarmId = alarmId,
    alarmHourOfDay = 7,
    alarmMinute = 0,
    nextAlarmTimeInMillis = 0,
    repeatingAlarmDays = null,
    numberOfSnoozes = 3,
    snoozeDurationInMinutes = 10,
    ringtone = if (hasCustomRingtoneFile) {
        Alarm.Ringtone.CUSTOM_SOUND.name
    } else {
        Alarm.Ringtone.GENTLE_GUITAR.name
    },
    hasCustomRingtoneFile = hasCustomRingtoneFile,
    alarmVolumePercentage = 0,
    areVibrationsEnabled = true,
    isUsingCode = false,
    assignedCodeId = null,
    isOpenCodeLinkEnabled = false,
    cancelLockDurationInMinutes = 0,
    isEmergencyTaskEnabled = false,
    alarmLabel = null,
    gentleWakeUpDurationInSeconds = 0,
    temporaryMuteDurationInSeconds = 0
)

fun backupPreferences(
    defaultAlarmCodeId: Long? = null,
    emergencySliderRange: IntRange? = null,
    emergencyRequiredMatches: Int? = null,
    emergencyDisableRepeatingAlarms: Boolean? = null,
    theme: Theme? = null
) = BackupPreferences(
    defaultAlarmCodeId = defaultAlarmCodeId,
    emergencySliderRange = emergencySliderRange,
    emergencyRequiredMatches = emergencyRequiredMatches,
    emergencyDisableRepeatingAlarms = emergencyDisableRepeatingAlarms,
    theme = theme
)

fun backupBundle(
    alarms: List<BackupAlarm> = emptyList(),
    codes: List<BackupCode> = emptyList(),
    preferences: BackupPreferences = backupPreferences()
) = BackupBundle(alarms = alarms, codes = codes, preferences = preferences)

fun backupMetadata() = BackupMetadata(
    backupFormatVersion = CURRENT_BACKUP_FORMAT_VERSION,
    createdAtEpochMillis = 0,
    applicationId = "com.sweak.qralarm",
    flavor = "master",
    versionCode = 1,
    versionName = "1.0.0",
    databaseSchemaVersion = 1
)
