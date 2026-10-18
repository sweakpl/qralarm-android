package com.sweak.qralarm.core.domain.fixture

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Code

fun alarm(
    isAlarmEnabled: Boolean = true,
    isAlarmRunning: Boolean = false,
    numberOfSnoozesLeft: Int = 3,
    isAlarmSnoozed: Boolean = false,
    nextSnoozedAlarmTimeInMillis: Long? = null,
    customRingtoneUriString: String? = null,
    assignedCode: Code? = null,
    skipAlarmUntilTimeInMillis: Long? = null
) = Alarm(
    alarmId = 0,
    alarmHourOfDay = 7,
    alarmMinute = 0,
    isAlarmEnabled = isAlarmEnabled,
    isAlarmRunning = isAlarmRunning,
    repeatingMode = Alarm.RepeatingMode.Once,
    nextAlarmTimeInMillis = 0,
    snoozeConfig = Alarm.SnoozeConfig(
        snoozeMode = Alarm.SnoozeMode(numberOfSnoozes = 3, snoozeDurationInMinutes = 10),
        numberOfSnoozesLeft = numberOfSnoozesLeft,
        isAlarmSnoozed = isAlarmSnoozed,
        nextSnoozedAlarmTimeInMillis = nextSnoozedAlarmTimeInMillis
    ),
    ringtone = if (customRingtoneUriString != null) {
        Alarm.Ringtone.CUSTOM_SOUND
    } else {
        Alarm.Ringtone.GENTLE_GUITAR
    },
    customRingtoneUriString = customRingtoneUriString,
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
    skipAlarmUntilTimeInMillis = skipAlarmUntilTimeInMillis
)
