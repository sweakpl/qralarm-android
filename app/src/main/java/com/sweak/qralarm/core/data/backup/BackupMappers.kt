package com.sweak.qralarm.core.data.backup

import android.os.Build
import com.sweak.qralarm.core.data.backup.dto.AlarmDto
import com.sweak.qralarm.core.data.backup.dto.AppDto
import com.sweak.qralarm.core.data.backup.dto.CodeDto
import com.sweak.qralarm.core.data.backup.dto.IntRangeDto
import com.sweak.qralarm.core.data.backup.dto.ManifestDto
import com.sweak.qralarm.core.data.backup.dto.PreferencesDto
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.backup.BackupAlarm
import com.sweak.qralarm.core.domain.backup.BackupCode
import com.sweak.qralarm.core.domain.backup.BackupMetadata
import com.sweak.qralarm.core.domain.backup.BackupPreferences
import com.sweak.qralarm.core.domain.user.model.Theme
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import com.sweak.qralarm.core.storage.database.model.CodeEntity
import java.time.DayOfWeek

fun AlarmEntity.toBackupAlarm(): BackupAlarm = BackupAlarm(
    alarmId = alarmId,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    repeatingAlarmDays = repeatingAlarmDays,
    numberOfSnoozes = numberOfSnoozes,
    snoozeDurationInMinutes = snoozeDurationInMinutes,
    ringtone = ringtone,
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

fun BackupMetadata.toManifestDto(): ManifestDto = ManifestDto(
    backupFormatVersion = backupFormatVersion,
    createdAtEpochMillis = createdAtEpochMillis,
    app = AppDto(
        applicationId = applicationId,
        flavor = flavor,
        versionCode = versionCode,
        versionName = versionName
    ),
    databaseSchemaVersion = databaseSchemaVersion
)

fun ManifestDto.toBackupMetadata(): BackupMetadata = BackupMetadata(
    backupFormatVersion = backupFormatVersion,
    createdAtEpochMillis = createdAtEpochMillis,
    applicationId = app.applicationId,
    flavor = app.flavor,
    versionCode = app.versionCode,
    versionName = app.versionName,
    databaseSchemaVersion = databaseSchemaVersion
)

fun BackupAlarm.toAlarmDto(): AlarmDto = AlarmDto(
    alarmId = alarmId,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    repeatingAlarmDays = repeatingAlarmDays
        ?.split(REPEATING_ALARM_DAYS_SEPARATOR)
        ?.filter { it.isNotBlank() },
    numberOfSnoozes = numberOfSnoozes,
    snoozeDurationInMinutes = snoozeDurationInMinutes,
    ringtone = ringtone,
    hasCustomRingtoneFile = hasCustomRingtoneFile,
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

fun AlarmDto.toBackupAlarm(): BackupAlarm = BackupAlarm(
    alarmId = alarmId,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    // An unknown day name is dropped; an alarm left without a single day it repeats on becomes an
    // alarm that does not repeat, which also keeps the empty string out of the database.
    repeatingAlarmDays = repeatingAlarmDays
        ?.mapNotNull { dayName -> DayOfWeek.entries.find { it.name == dayName } }
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(REPEATING_ALARM_DAYS_SEPARATOR) { it.name },
    numberOfSnoozes = numberOfSnoozes,
    snoozeDurationInMinutes = snoozeDurationInMinutes,
    ringtone = (Alarm.Ringtone.entries.find { it.name == ringtone }
        ?: Alarm.Ringtone.GENTLE_GUITAR).name,
    hasCustomRingtoneFile = hasCustomRingtoneFile,
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

fun BackupCode.toCodeDto(): CodeDto = CodeDto(
    codeId = codeId,
    value = value,
    name = name
)

fun CodeDto.toBackupCode(): BackupCode = BackupCode(
    codeId = codeId,
    value = value,
    name = name
)

fun BackupPreferences.toPreferencesDto(): PreferencesDto = PreferencesDto(
    defaultAlarmCodeId = defaultAlarmCodeId,
    emergencySliderRange = emergencySliderRange?.let {
        IntRangeDto(first = it.first, last = it.last)
    },
    emergencyRequiredMatches = emergencyRequiredMatches,
    emergencyDisableRepeatingAlarms = emergencyDisableRepeatingAlarms,
    theme = theme
)

fun PreferencesDto.toBackupPreferences(): BackupPreferences = BackupPreferences(
    defaultAlarmCodeId = defaultAlarmCodeId,
    emergencySliderRange = emergencySliderRange?.let { it.first..it.last },
    emergencyRequiredMatches = emergencyRequiredMatches,
    emergencyDisableRepeatingAlarms = emergencyDisableRepeatingAlarms,
    theme = if (theme is Theme.Dynamic && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Theme.Default
    } else {
        theme
    }
)

/** How the alarm's repeating days are stored in one column, see AlarmsRepositoryImpl. */
private const val REPEATING_ALARM_DAYS_SEPARATOR = ", "
