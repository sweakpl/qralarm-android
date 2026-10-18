package com.sweak.qralarm.core.domain.fixture

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Code

fun alarm(
    alarmHourOfDay: Int = 7,
    alarmMinute: Int = 0,
    isAlarmEnabled: Boolean = true,
    isAlarmRunning: Boolean = false,
    repeatingMode: Alarm.RepeatingMode = Alarm.RepeatingMode.Once,
    nextAlarmTimeInMillis: Long = 0,
    numberOfSnoozes: Int = 3,
    snoozeDurationInMinutes: Int = 10,
    numberOfSnoozesLeft: Int = 3,
    isAlarmSnoozed: Boolean = false,
    nextSnoozedAlarmTimeInMillis: Long? = null,
    customRingtoneUriString: String? = null,
    ringtone: Alarm.Ringtone = if (customRingtoneUriString != null) {
        Alarm.Ringtone.CUSTOM_SOUND
    } else {
        Alarm.Ringtone.GENTLE_GUITAR
    },
    assignedCode: Code? = null,
    isUsingCode: Boolean = assignedCode != null,
    cancelLockDurationInMinutes: Int = 0,
    skipAlarmUntilTimeInMillis: Long? = null
) = Alarm(
    alarmId = 0,
    alarmHourOfDay = alarmHourOfDay,
    alarmMinute = alarmMinute,
    isAlarmEnabled = isAlarmEnabled,
    isAlarmRunning = isAlarmRunning,
    repeatingMode = repeatingMode,
    nextAlarmTimeInMillis = nextAlarmTimeInMillis,
    snoozeConfig = Alarm.SnoozeConfig(
        snoozeMode = Alarm.SnoozeMode(
            numberOfSnoozes = numberOfSnoozes,
            snoozeDurationInMinutes = snoozeDurationInMinutes
        ),
        numberOfSnoozesLeft = numberOfSnoozesLeft,
        isAlarmSnoozed = isAlarmSnoozed,
        nextSnoozedAlarmTimeInMillis = nextSnoozedAlarmTimeInMillis
    ),
    ringtone = ringtone,
    customRingtoneUriString = customRingtoneUriString,
    alarmVolumeMode = Alarm.AlarmVolumeMode.System,
    areVibrationsEnabled = true,
    isUsingCode = isUsingCode,
    assignedCode = assignedCode,
    isOpenCodeLinkEnabled = false,
    cancelLockDurationInMinutes = cancelLockDurationInMinutes,
    isEmergencyTaskEnabled = false,
    alarmLabel = null,
    gentleWakeUpDurationInSeconds = 0,
    temporaryMuteDurationInSeconds = 0,
    skipAlarmUntilTimeInMillis = skipAlarmUntilTimeInMillis
)
