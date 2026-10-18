package com.sweak.qralarm.core.domain.fixture

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.backup.BackupAlarm

fun backupAlarm(alarmId: Long) = BackupAlarm(
    alarmId = alarmId,
    alarmHourOfDay = 7,
    alarmMinute = 0,
    nextAlarmTimeInMillis = 0,
    repeatingAlarmDays = null,
    numberOfSnoozes = 3,
    snoozeDurationInMinutes = 10,
    ringtone = Alarm.Ringtone.GENTLE_GUITAR.name,
    hasCustomRingtoneFile = false,
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
