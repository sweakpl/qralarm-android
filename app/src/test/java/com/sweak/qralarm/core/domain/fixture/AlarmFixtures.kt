package com.sweak.qralarm.core.domain.fixture

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Code

fun alarm(
    assignedCode: Code? = null
) = Alarm(
    alarmId = 0,
    alarmHourOfDay = 7,
    alarmMinute = 0,
    isAlarmEnabled = true,
    isAlarmRunning = false,
    repeatingMode = Alarm.RepeatingMode.Once,
    nextAlarmTimeInMillis = 0,
    snoozeConfig = Alarm.SnoozeConfig(
        snoozeMode = Alarm.SnoozeMode(numberOfSnoozes = 3, snoozeDurationInMinutes = 10),
        numberOfSnoozesLeft = 3,
        isAlarmSnoozed = false,
        nextSnoozedAlarmTimeInMillis = null
    ),
    ringtone = Alarm.Ringtone.GENTLE_GUITAR,
    customRingtoneUriString = null,
    alarmVolumeMode = Alarm.AlarmVolumeMode.System,
    areVibrationsEnabled = true,
    isUsingCode = assignedCode != null,
    assignedCode = assignedCode,
    isOpenCodeLinkEnabled = false,
    cancelLockDurationInMinutes = 0,
    isEmergencyTaskEnabled = false,
    alarmLabel = null,
    gentleWakeUpDurationInSeconds = 0,
    temporaryMuteDurationInSeconds = 0,
    skipAlarmUntilTimeInMillis = null
)
