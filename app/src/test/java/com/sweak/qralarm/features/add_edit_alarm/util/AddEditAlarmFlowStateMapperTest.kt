package com.sweak.qralarm.features.add_edit_alarm.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowState
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import com.sweak.qralarm.core.domain.alarm.Code as DomainCode

class AddEditAlarmFlowStateMapperTest {

    private fun mapper(now: LocalDateTime): AddEditAlarmFlowStateMapper {
        val clock = fixedClock(now)

        return AddEditAlarmFlowStateMapper(
            alarmScheduleCalculator = AlarmScheduleCalculator(clock = clock),
            clock = clock
        )
    }

    private fun existingAlarmState(
        alarm: Alarm,
        now: LocalDateTime,
        currentState: AddEditAlarmFlowState = AddEditAlarmFlowState(),
        alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper =
            AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE),
        previouslySavedCodes: List<Code> = emptyList(),
        isSystemDefaultAlarmRingtoneAvailable: Boolean = true
    ) = mapper(now).withExistingAlarm(
        currentState = currentState,
        alarm = alarm,
        alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
        availableRingtonesWithPlaybackState = emptyMap(),
        previouslySavedCodes = previouslySavedCodes,
        isSystemDefaultAlarmRingtoneAvailable = isSystemDefaultAlarmRingtoneAvailable
    )

    @Test
    fun `given new alarm, when mapping, then alarm is preset to one minute from now as a one-time alarm`() {
        // Given
        val mapper = mapper(now = SATURDAY.atTime(21, 44))
        val savedCodes = listOf(Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"))

        // When
        val result = mapper.withNewAlarm(
            currentState = AddEditAlarmFlowState(),
            availableRingtonesWithPlaybackState = mapOf(Ringtone.KALIMBA to false),
            previouslySavedCodes = savedCodes
        )

        // Then
        assertThat(result.isLoading).isFalse()
        assertThat(result.isEditingExistingAlarm).isFalse()
        assertThat(result.alarmHourOfDay).isEqualTo(21)
        assertThat(result.alarmMinute).isEqualTo(45)
        assertThat(result.alarmRepeatingScheduleWrapper)
            .isEqualTo(AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE))
        assertThat(result.onlyOnceAlarmDateInMillis)
            .isEqualTo(epochMillisAt(SATURDAY.atTime(21, 45)))
        assertThat(result.daysHoursAndMinutesUntilAlarm).isEqualTo(Triple(0, 0, 1))
        assertThat(result.availableRingtonesWithPlaybackState)
            .isEqualTo(mapOf(Ringtone.KALIMBA to false))
        assertThat(result.previouslySavedCodes).isEqualTo(savedCodes)
    }

    @Test
    fun `given one-time alarm stored for a date ahead, when mapping, then the date is kept and is not sticky`() {
        // Given
        val alarm = alarm(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            repeatingMode = Alarm.RepeatingMode.Once,
            nextAlarmTimeInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30))
        )

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            alarmRepeatingScheduleWrapper =
                AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE)
        )

        // Then
        assertThat(result.isLoading).isFalse()
        assertThat(result.isEditingExistingAlarm).isTrue()
        assertThat(result.alarmHourOfDay).isEqualTo(7)
        assertThat(result.alarmMinute).isEqualTo(30)
        assertThat(result.isOnlyOnceAlarmDateSticky).isFalse()
        assertThat(result.onlyOnceAlarmDateInMillis)
            .isEqualTo(epochMillisAt(WEDNESDAY.atTime(7, 30)))
        assertThat(result.daysHoursAndMinutesUntilAlarm).isEqualTo(Triple(3, 21, 30))
    }

    @Test
    fun `given one-time alarm stored for a date already passed, when mapping, then the date sticks to the earliest possible one`() {
        // Given
        val alarm = alarm(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            repeatingMode = Alarm.RepeatingMode.Once,
            nextAlarmTimeInMillis = epochMillisAt(FRIDAY.atTime(7, 30))
        )

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            alarmRepeatingScheduleWrapper =
                AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE)
        )

        // Then
        assertThat(result.isOnlyOnceAlarmDateSticky).isTrue()
        assertThat(result.onlyOnceAlarmDateInMillis)
            .isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
        assertThat(result.daysHoursAndMinutesUntilAlarm).isEqualTo(Triple(0, 21, 30))
    }

    @Test
    fun `given repeating alarm, when mapping, then one-time date sticks to the earliest possible one and countdown targets the next repeating day`() {
        // Given
        val alarm = alarm(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            repeatingMode = Alarm.RepeatingMode.Days(
                repeatingDaysOfWeek = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            ),
            nextAlarmTimeInMillis = epochMillisAt(MONDAY.atTime(7, 30))
        )

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(6, 0),
            alarmRepeatingScheduleWrapper =
                AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI)
        )

        // Then
        assertThat(result.alarmRepeatingScheduleWrapper)
            .isEqualTo(AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI))
        assertThat(result.isOnlyOnceAlarmDateSticky).isTrue()
        assertThat(result.onlyOnceAlarmDateInMillis)
            .isEqualTo(epochMillisAt(SATURDAY.atTime(7, 30)))
        assertThat(result.daysHoursAndMinutesUntilAlarm).isEqualTo(Triple(2, 1, 30))
    }

    @Test
    fun `given alarm using system default ringtone that is no longer available, when mapping, then ringtone falls back to gentle guitar`() {
        // Given
        val alarm = alarm(ringtone = Ringtone.SYSTEM_DEFAULT)

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            isSystemDefaultAlarmRingtoneAvailable = false
        )

        // Then
        assertThat(result.ringtone).isEqualTo(Ringtone.GENTLE_GUITAR)
    }

    @Test
    fun `given alarm using system default ringtone that is available, when mapping, then ringtone is kept`() {
        // Given
        val alarm = alarm(ringtone = Ringtone.SYSTEM_DEFAULT)

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            isSystemDefaultAlarmRingtoneAvailable = true
        )

        // Then
        assertThat(result.ringtone).isEqualTo(Ringtone.SYSTEM_DEFAULT)
    }

    @Test
    fun `given alarm with customized settings, when mapping, then settings are carried over to the state`() {
        // Given
        val alarm = alarm(
            numberOfSnoozes = 2,
            snoozeDurationInMinutes = 5,
            ringtone = Ringtone.ROOSTER,
            customRingtoneUriString = null,
            assignedCode = DomainCode(codeId = 5, value = "BATHROOM-QR", name = "Bathroom"),
            isUsingCode = true,
            cancelLockDurationInMinutes = 30
        ).copy(
            alarmVolumeMode = Alarm.AlarmVolumeMode.Custom(volumePercentage = 60),
            areVibrationsEnabled = false,
            isOpenCodeLinkEnabled = true,
            isEmergencyTaskEnabled = true,
            alarmLabel = "Gym",
            gentleWakeUpDurationInSeconds = 30,
            temporaryMuteDurationInSeconds = 45
        )
        val savedCodes = listOf(Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"))

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            previouslySavedCodes = savedCodes
        )

        // Then
        assertThat(result.snoozeNumberToDurationPair).isEqualTo(2 to 5)
        assertThat(result.ringtone).isEqualTo(Ringtone.ROOSTER)
        assertThat(result.currentCustomAlarmRingtoneUri).isNull()
        assertThat(result.alarmVolumePercentage).isEqualTo(60)
        assertThat(result.areVibrationsEnabled).isFalse()
        assertThat(result.isCodeEnabled).isTrue()
        assertThat(result.currentlyAssignedCode)
            .isEqualTo(Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"))
        assertThat(result.previouslySavedCodes).isEqualTo(savedCodes)
        assertThat(result.isOpenCodeLinkEnabled).isTrue()
        assertThat(result.cancelLockDurationInMinutes).isEqualTo(30)
        assertThat(result.isEmergencyTaskEnabled).isTrue()
        assertThat(result.alarmLabel).isEqualTo("Gym")
        assertThat(result.gentleWakeupDurationInSeconds).isEqualTo(30)
        assertThat(result.temporaryMuteDurationInSeconds).isEqualTo(45)
    }

    @Test
    fun `given alarm using system volume, when mapping, then there is no custom volume percentage`() {
        // Given
        val alarm = alarm().copy(alarmVolumeMode = Alarm.AlarmVolumeMode.System)

        // When
        val result = existingAlarmState(alarm = alarm, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result.alarmVolumePercentage).isNull()
    }

    @Test
    fun `given code scanned while the alarm was loading, when mapping, then the scanned code is preserved`() {
        // Given
        val scannedCode = Code(id = 0, value = "KITCHEN-QR", name = null)
        val alarm = alarm(
            assignedCode = DomainCode(codeId = 5, value = "BATHROOM-QR", name = "Bathroom")
        )

        // When
        val result = existingAlarmState(
            alarm = alarm,
            now = SATURDAY.atTime(10, 0),
            currentState = AddEditAlarmFlowState(temporaryAssignedCode = scannedCode)
        )

        // Then
        assertThat(result.temporaryAssignedCode).isEqualTo(scannedCode)
        assertThat(result.currentlyAssignedCode)
            .isEqualTo(Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"))
    }

    @Test
    fun `given state without alarm time, when mapping to alarm, then there is no alarm`() {
        // Given
        val state = AddEditAlarmFlowState(alarmHourOfDay = null, alarmMinute = null)

        // When
        val result = mapper(now = SATURDAY.atTime(10, 0)).toAlarm(state = state, alarmId = 42)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `given fully configured one-time alarm state, when mapping to alarm, then alarm reflects the state and is scheduled for the chosen date`() {
        // Given
        val state = AddEditAlarmFlowState(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            alarmRepeatingScheduleWrapper =
                AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE),
            onlyOnceAlarmDateInMillis = epochMillisAt(WEDNESDAY.atTime(9, 15)),
            snoozeNumberToDurationPair = 2 to 5,
            ringtone = Ringtone.ROOSTER,
            currentCustomAlarmRingtoneUri = null,
            alarmVolumePercentage = 60,
            areVibrationsEnabled = false,
            isCodeEnabled = true,
            currentlyAssignedCode = Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"),
            temporaryAssignedCode = null,
            isOpenCodeLinkEnabled = true,
            cancelLockDurationInMinutes = 30,
            isEmergencyTaskEnabled = false,
            alarmLabel = "Gym",
            gentleWakeupDurationInSeconds = 30,
            temporaryMuteDurationInSeconds = 45
        )

        // When
        val result = mapper(now = SATURDAY.atTime(10, 0)).toAlarm(state = state, alarmId = 42)

        // Then
        assertThat(result).isEqualTo(
            Alarm(
                alarmId = 42,
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                isAlarmRunning = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30)),
                snoozeConfig = Alarm.SnoozeConfig(
                    snoozeMode = Alarm.SnoozeMode(
                        numberOfSnoozes = 2,
                        snoozeDurationInMinutes = 5
                    ),
                    numberOfSnoozesLeft = 2,
                    isAlarmSnoozed = false,
                    nextSnoozedAlarmTimeInMillis = null
                ),
                ringtone = Ringtone.ROOSTER,
                customRingtoneUriString = null,
                alarmVolumeMode = Alarm.AlarmVolumeMode.Custom(volumePercentage = 60),
                areVibrationsEnabled = false,
                isUsingCode = true,
                assignedCode = DomainCode(codeId = 5, value = "BATHROOM-QR", name = "Bathroom"),
                isOpenCodeLinkEnabled = true,
                cancelLockDurationInMinutes = 30,
                isEmergencyTaskEnabled = false,
                alarmLabel = "Gym",
                gentleWakeUpDurationInSeconds = 30,
                temporaryMuteDurationInSeconds = 45,
                skipAlarmUntilTimeInMillis = null
            )
        )
    }

    @Test
    fun `given Monday to Friday alarm state on Saturday, when mapping to alarm, then alarm repeats on working days and is scheduled for Monday`() {
        // Given
        val state = AddEditAlarmFlowState(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            alarmRepeatingScheduleWrapper =
                AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI),
            onlyOnceAlarmDateInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
        )

        // When
        val result = mapper(now = SATURDAY.atTime(6, 0)).toAlarm(state = state, alarmId = 42)

        // Then
        assertThat(result?.repeatingMode).isEqualTo(
            Alarm.RepeatingMode.Days(
                repeatingDaysOfWeek = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
            )
        )
        assertThat(result?.nextAlarmTimeInMillis).isEqualTo(epochMillisAt(MONDAY.atTime(7, 30)))
    }

    @Test
    fun `given state with both a previously assigned and a newly scanned code, when mapping to alarm, then the newly scanned code wins`() {
        // Given
        val state = AddEditAlarmFlowState(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            currentlyAssignedCode = Code(id = 5, value = "BATHROOM-QR", name = "Bathroom"),
            temporaryAssignedCode = Code(id = 0, value = "KITCHEN-QR", name = null)
        )

        // When
        val result = mapper(now = SATURDAY.atTime(6, 0)).toAlarm(state = state, alarmId = 42)

        // Then
        assertThat(result?.assignedCode)
            .isEqualTo(DomainCode(codeId = 0, value = "KITCHEN-QR", name = null))
    }

    @Test
    fun `given state using system volume, when mapping to alarm, then alarm uses system volume mode`() {
        // Given
        val state = AddEditAlarmFlowState(
            alarmHourOfDay = 7,
            alarmMinute = 30,
            alarmVolumePercentage = null
        )

        // When
        val result = mapper(now = SATURDAY.atTime(6, 0)).toAlarm(state = state, alarmId = 42)

        // Then
        assertThat(result?.alarmVolumeMode).isEqualTo(Alarm.AlarmVolumeMode.System)
    }

    private companion object {
        val FRIDAY: LocalDate = LocalDate.of(2026, 10, 16)
        val SATURDAY: LocalDate = LocalDate.of(2026, 10, 17)
        val SUNDAY: LocalDate = LocalDate.of(2026, 10, 18)
        val MONDAY: LocalDate = LocalDate.of(2026, 10, 19)
        val WEDNESDAY: LocalDate = LocalDate.of(2026, 10, 21)
    }
}
