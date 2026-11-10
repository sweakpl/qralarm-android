package com.sweak.qralarm.features.add_edit_alarm.util

import androidx.core.net.toUri
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.getEarliestOnlyOnceAlarmDate
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import com.sweak.qralarm.core.domain.alarm.Code as DomainCode

class AlarmDraftMapper @Inject constructor(
    private val alarmScheduleCalculator: AlarmScheduleCalculator,
    private val clock: Clock
) {
    fun withNewAlarm(currentDraft: AlarmDraft): AlarmDraft {
        val dateTime = ZonedDateTime.now(clock).plusMinutes(1)
        val onlyOnceAlarmDateInMillis = alarmScheduleCalculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = 0L,
            hourOfDay = dateTime.hour,
            minute = dateTime.minute,
            isDateSticky = true
        )

        return currentDraft.copy(
            alarmHourOfDay = dateTime.hour,
            alarmMinute = dateTime.minute,
            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
            alarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper()
        )
    }

    fun withExistingAlarm(
        currentDraft: AlarmDraft,
        alarm: Alarm,
        alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
        isSystemDefaultAlarmRingtoneAvailable: Boolean
    ): AlarmDraft {
        val isDateSticky: Boolean
        val onlyOnceAlarmDateInMillis: Long
        val earliestDate = getEarliestOnlyOnceAlarmDate(
            hourOfDay = alarm.alarmHourOfDay,
            minute = alarm.alarmMinute,
            now = ZonedDateTime.now(clock)
        )

        if (alarm.repeatingMode == Alarm.RepeatingMode.Once) {
            val persistedDate = Instant.ofEpochMilli(alarm.nextAlarmTimeInMillis)
                .atZone(clock.zone)
                .toLocalDate()
            isDateSticky = !persistedDate.isAfter(earliestDate)
            onlyOnceAlarmDateInMillis = alarmScheduleCalculator.resolveOnlyOnceAlarmDateInMillis(
                currentDateInMillis = alarm.nextAlarmTimeInMillis,
                hourOfDay = alarm.alarmHourOfDay,
                minute = alarm.alarmMinute,
                isDateSticky = isDateSticky
            )
        } else {
            isDateSticky = true
            onlyOnceAlarmDateInMillis = earliestDate
                .atTime(alarm.alarmHourOfDay, alarm.alarmMinute)
                .atZone(clock.zone)
                .toInstant()
                .toEpochMilli()
        }

        val savedRingtone =
            if (alarm.ringtone == Ringtone.SYSTEM_DEFAULT && !isSystemDefaultAlarmRingtoneAvailable) {
                Ringtone.GENTLE_GUITAR
            } else {
                alarm.ringtone
            }

        return currentDraft.copy(
            alarmHourOfDay = alarm.alarmHourOfDay,
            alarmMinute = alarm.alarmMinute,
            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
            isOnlyOnceAlarmDateSticky = isDateSticky,
            alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
            snoozeNumberToDurationPair = Pair(
                first = alarm.snoozeConfig.snoozeMode.numberOfSnoozes,
                second = alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes
            ),
            ringtone = savedRingtone,
            currentCustomAlarmRingtoneUri = alarm.customRingtoneUriString?.toUri(),
            alarmVolumePercentage =
                if (alarm.alarmVolumeMode is Alarm.AlarmVolumeMode.Custom) {
                    alarm.alarmVolumeMode.volumePercentage
                } else {
                    null
                },
            areVibrationsEnabled = alarm.areVibrationsEnabled,
            isCodeEnabled = alarm.isUsingCode,
            currentlyAssignedCode = alarm.assignedCode?.let {
                Code(id = it.codeId, value = it.value, name = it.name)
            },
            isOpenCodeLinkEnabled = alarm.isOpenCodeLinkEnabled,
            cancelLockDurationInMinutes = alarm.cancelLockDurationInMinutes,
            isEmergencyTaskEnabled = alarm.isEmergencyTaskEnabled,
            alarmLabel = alarm.alarmLabel,
            gentleWakeupDurationInSeconds = alarm.gentleWakeUpDurationInSeconds,
            temporaryMuteDurationInSeconds = alarm.temporaryMuteDurationInSeconds
        )
    }

    fun toAlarm(draft: AlarmDraft, alarmId: Long): Alarm? {
        val alarmHourOfDay = draft.alarmHourOfDay ?: return null
        val alarmMinute = draft.alarmMinute ?: return null

        return Alarm(
            alarmId = alarmId,
            alarmHourOfDay = alarmHourOfDay,
            alarmMinute = alarmMinute,
            isAlarmEnabled = true,
            isAlarmRunning = false,
            repeatingMode = draft.alarmRepeatingScheduleWrapper.toRepeatingMode(),
            nextAlarmTimeInMillis = alarmScheduleCalculator.computeNextAlarmTimeInMillis(
                hourOfDay = alarmHourOfDay,
                minute = alarmMinute,
                repeatingScheduleWrapper = draft.alarmRepeatingScheduleWrapper,
                onlyOnceAlarmDateInMillis = draft.onlyOnceAlarmDateInMillis
            ),
            snoozeConfig = Alarm.SnoozeConfig(
                snoozeMode = Alarm.SnoozeMode(
                    numberOfSnoozes = draft.snoozeNumberToDurationPair.first,
                    snoozeDurationInMinutes = draft.snoozeNumberToDurationPair.second
                ),
                numberOfSnoozesLeft = draft.snoozeNumberToDurationPair.first,
                isAlarmSnoozed = false,
                nextSnoozedAlarmTimeInMillis = null
            ),
            ringtone = draft.ringtone,
            customRingtoneUriString = draft.currentCustomAlarmRingtoneUri?.toString(),
            alarmVolumeMode = if (draft.alarmVolumePercentage != null) {
                Alarm.AlarmVolumeMode.Custom(volumePercentage = draft.alarmVolumePercentage)
            } else {
                Alarm.AlarmVolumeMode.System
            },
            areVibrationsEnabled = draft.areVibrationsEnabled,
            isUsingCode = draft.isCodeEnabled,
            assignedCode = (draft.temporaryAssignedCode ?: draft.currentlyAssignedCode)
                ?.let { uiCode ->
                    DomainCode(codeId = uiCode.id, value = uiCode.value, name = uiCode.name)
                },
            isOpenCodeLinkEnabled = draft.isOpenCodeLinkEnabled,
            cancelLockDurationInMinutes = draft.cancelLockDurationInMinutes,
            isEmergencyTaskEnabled = draft.isEmergencyTaskEnabled,
            alarmLabel = draft.alarmLabel,
            gentleWakeUpDurationInSeconds = draft.gentleWakeupDurationInSeconds,
            temporaryMuteDurationInSeconds = draft.temporaryMuteDurationInSeconds,
            skipAlarmUntilTimeInMillis = null
        )
    }
}
