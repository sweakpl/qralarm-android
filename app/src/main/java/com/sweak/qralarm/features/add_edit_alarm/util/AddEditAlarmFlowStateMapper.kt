package com.sweak.qralarm.features.add_edit_alarm.util

import androidx.core.net.toUri
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.getDaysHoursAndMinutesUntilAlarm
import com.sweak.qralarm.core.ui.getEarliestOnlyOnceAlarmDate
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowState
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import com.sweak.qralarm.core.domain.alarm.Code as DomainCode

class AddEditAlarmFlowStateMapper @Inject constructor(
    private val alarmScheduleCalculator: AlarmScheduleCalculator,
    private val clock: Clock
) {
    fun withNewAlarm(
        currentState: AddEditAlarmFlowState,
        availableRingtonesWithPlaybackState: Map<Ringtone, Boolean>,
        previouslySavedCodes: List<Code>
    ): AddEditAlarmFlowState {
        val dateTime = ZonedDateTime.now(clock).plusMinutes(1)
        val defaultRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper()
        val onlyOnceAlarmDateInMillis = alarmScheduleCalculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = 0L,
            hourOfDay = dateTime.hour,
            minute = dateTime.minute,
            isDateSticky = true
        )

        return currentState.copy(
            isLoading = false,
            isEditingExistingAlarm = false,
            alarmHourOfDay = dateTime.hour,
            alarmMinute = dateTime.minute,
            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
            daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilNextAlarm(
                hourOfDay = dateTime.hour,
                minute = dateTime.minute,
                repeatingScheduleWrapper = defaultRepeatingScheduleWrapper,
                onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis
            ),
            alarmRepeatingScheduleWrapper = defaultRepeatingScheduleWrapper,
            availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState,
            previouslySavedCodes = previouslySavedCodes
        )
    }

    fun withExistingAlarm(
        currentState: AddEditAlarmFlowState,
        alarm: Alarm,
        alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
        availableRingtonesWithPlaybackState: Map<Ringtone, Boolean>,
        previouslySavedCodes: List<Code>,
        isSystemDefaultAlarmRingtoneAvailable: Boolean
    ): AddEditAlarmFlowState {
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

        return currentState.copy(
            isLoading = false,
            isEditingExistingAlarm = true,
            alarmHourOfDay = alarm.alarmHourOfDay,
            alarmMinute = alarm.alarmMinute,
            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis,
            isOnlyOnceAlarmDateSticky = isDateSticky,
            daysHoursAndMinutesUntilAlarm = getDaysHoursAndMinutesUntilNextAlarm(
                hourOfDay = alarm.alarmHourOfDay,
                minute = alarm.alarmMinute,
                repeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis
            ),
            alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
            snoozeNumberToDurationPair = Pair(
                first = alarm.snoozeConfig.snoozeMode.numberOfSnoozes,
                second = alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes
            ),
            availableRingtonesWithPlaybackState = availableRingtonesWithPlaybackState,
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
            previouslySavedCodes = previouslySavedCodes,
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

    fun toAlarm(state: AddEditAlarmFlowState, alarmId: Long): Alarm? {
        val alarmHourOfDay = state.alarmHourOfDay ?: return null
        val alarmMinute = state.alarmMinute ?: return null

        return Alarm(
            alarmId = alarmId,
            alarmHourOfDay = alarmHourOfDay,
            alarmMinute = alarmMinute,
            isAlarmEnabled = true,
            isAlarmRunning = false,
            repeatingMode = state.alarmRepeatingScheduleWrapper.toRepeatingMode(),
            nextAlarmTimeInMillis = alarmScheduleCalculator.computeNextAlarmTimeInMillis(
                hourOfDay = alarmHourOfDay,
                minute = alarmMinute,
                repeatingScheduleWrapper = state.alarmRepeatingScheduleWrapper,
                onlyOnceAlarmDateInMillis = state.onlyOnceAlarmDateInMillis
            ),
            snoozeConfig = Alarm.SnoozeConfig(
                snoozeMode = Alarm.SnoozeMode(
                    numberOfSnoozes = state.snoozeNumberToDurationPair.first,
                    snoozeDurationInMinutes = state.snoozeNumberToDurationPair.second
                ),
                numberOfSnoozesLeft = state.snoozeNumberToDurationPair.first,
                isAlarmSnoozed = false,
                nextSnoozedAlarmTimeInMillis = null
            ),
            ringtone = state.ringtone,
            customRingtoneUriString = state.currentCustomAlarmRingtoneUri?.toString(),
            alarmVolumeMode = if (state.alarmVolumePercentage != null) {
                Alarm.AlarmVolumeMode.Custom(volumePercentage = state.alarmVolumePercentage)
            } else {
                Alarm.AlarmVolumeMode.System
            },
            areVibrationsEnabled = state.areVibrationsEnabled,
            isUsingCode = state.isCodeEnabled,
            assignedCode = (state.temporaryAssignedCode ?: state.currentlyAssignedCode)
                ?.let { uiCode ->
                    DomainCode(codeId = uiCode.id, value = uiCode.value, name = uiCode.name)
                },
            isOpenCodeLinkEnabled = state.isOpenCodeLinkEnabled,
            cancelLockDurationInMinutes = state.cancelLockDurationInMinutes,
            isEmergencyTaskEnabled = state.isEmergencyTaskEnabled,
            alarmLabel = state.alarmLabel,
            gentleWakeUpDurationInSeconds = state.gentleWakeupDurationInSeconds,
            temporaryMuteDurationInSeconds = state.temporaryMuteDurationInSeconds,
            skipAlarmUntilTimeInMillis = null
        )
    }

    private fun getDaysHoursAndMinutesUntilNextAlarm(
        hourOfDay: Int,
        minute: Int,
        repeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
        onlyOnceAlarmDateInMillis: Long
    ): Triple<Int, Int, Int> = getDaysHoursAndMinutesUntilAlarm(
        alarmTimeInMillis = alarmScheduleCalculator.computeNextAlarmTimeInMillis(
            hourOfDay = hourOfDay,
            minute = minute,
            repeatingScheduleWrapper = repeatingScheduleWrapper,
            onlyOnceAlarmDateInMillis = onlyOnceAlarmDateInMillis
        ),
        currentTimeInMillis = clock.millis()
    )
}
