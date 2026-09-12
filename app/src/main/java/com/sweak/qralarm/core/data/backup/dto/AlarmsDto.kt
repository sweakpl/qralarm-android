package com.sweak.qralarm.core.data.backup.dto

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.DEFAULT_CANCEL_LOCK_DURATION_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.DEFAULT_GENTLE_WAKE_UP_DURATION_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.DEFAULT_SNOOZE_NUMBER_TO_DURATION_PAIR
import com.sweak.qralarm.core.domain.alarm.DEFAULT_TEMPORARY_MUTE_DURATION_IN_SECONDS
import kotlinx.serialization.Serializable

/** alarms.json - the alarms. */
@Serializable
data class AlarmsDto(
    val alarms: List<AlarmDto> = emptyList()
)

/**
 * Every field has a default, so that a backup made by a different version of the app - one that
 * did not write a field yet, or one that writes fields this version does not know - still reads.
 *
 * An alarm's runtime state has no fields here at all: a restored alarm is always switched off,
 * not running and not snoozed, so there is nothing to carry.
 */
@Serializable
data class AlarmDto(
    val alarmId: Long = 0,
    val alarmHourOfDay: Int = 0,
    val alarmMinute: Int = 0,
    /** For an alarm that does not repeat this also carries the date it is set for. */
    val nextAlarmTimeInMillis: Long = 0,
    /** [java.time.DayOfWeek] names, or null for an alarm that does not repeat. */
    val repeatingAlarmDays: List<String>? = null,
    val numberOfSnoozes: Int = DEFAULT_SNOOZE_NUMBER_TO_DURATION_PAIR.first,
    val snoozeDurationInMinutes: Int = DEFAULT_SNOOZE_NUMBER_TO_DURATION_PAIR.second,
    /** An [Alarm.Ringtone] name. */
    val ringtone: String = Alarm.Ringtone.GENTLE_GUITAR.name,
    /**
     * True if the alarm used a custom ringtone. Its file is under ringtones/[alarmId], unless it
     * could no longer be read when the backup was made.
     */
    val hasCustomRingtoneFile: Boolean = false,
    val alarmVolumePercentage: Int = 0,
    val areVibrationsEnabled: Boolean = true,
    val isUsingCode: Boolean = false,
    val assignedCodeId: Long? = null,
    val isOpenCodeLinkEnabled: Boolean = false,
    val cancelLockDurationInMinutes: Int = DEFAULT_CANCEL_LOCK_DURATION_IN_MINUTES,
    val isEmergencyTaskEnabled: Boolean = true,
    val alarmLabel: String? = null,
    val gentleWakeUpDurationInSeconds: Int = DEFAULT_GENTLE_WAKE_UP_DURATION_IN_SECONDS,
    val temporaryMuteDurationInSeconds: Int = DEFAULT_TEMPORARY_MUTE_DURATION_IN_SECONDS
)
