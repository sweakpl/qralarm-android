package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.domain.user.UserDataRepository
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class DismissAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)
    private val userDataRepository = mockk<UserDataRepository>()

    private suspend fun dismissAlarm(
        alarmId: Long,
        reason: DismissAlarm.Reason,
        now: LocalDateTime
    ): DismissAlarm.Result {
        val disableAlarm = DisableAlarm(
            alarmScheduler = alarmScheduler,
            alarmsRepository = alarmsRepository
        )

        return DismissAlarm(
            alarmsRepository = alarmsRepository,
            disableAlarm = disableAlarm,
            advanceAlarmToNextOccurrence = AdvanceAlarmToNextOccurrence(
                alarmsRepository = alarmsRepository,
                setAlarm = SetAlarm(
                    alarmScheduler = alarmScheduler,
                    alarmNotifier = alarmNotifier,
                    alarmsRepository = alarmsRepository,
                    clock = fixedClock(now)
                ),
                disableAlarm = disableAlarm
            ),
            userDataRepository = userDataRepository
        )(alarmId = alarmId, reason = reason)
    }

    private suspend fun storedAlarm(alarmId: Long): Alarm = alarmsRepository.getAlarm(alarmId)!!

    private fun verifyScheduledForNextMonday(alarmId: Long) {
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(NEXT_MONDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    private fun givenEmergencyDisablesRepeatingAlarms(enabled: Boolean) {
        every { userDataRepository.isEmergencyDisableRepeatingAlarmsEnabled } returns flowOf(enabled)
    }

    @Test
    fun `given no alarm with the id, when dismissing, then it is finished and its scheduled alarm is cancelled`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        val result = dismissAlarm(
            missingAlarmId,
            DismissAlarm.Reason.CodeScanned,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        verify { alarmScheduler.cancelAlarm(missingAlarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    // Stopped without code

    @Test
    fun `given ringing one-time alarm, when stopped without code, then it is disabled and cancelled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(isAlarmEnabled = true, repeatingMode = Alarm.RepeatingMode.Once)
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.StoppedWithoutCode,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given snoozed repeating alarm, when stopped without code, then it is un-snoozed and set for its next occurrence`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        dismissAlarm(
            alarmId,
            DismissAlarm.Reason.StoppedWithoutCode,
            now = SATURDAY.atTime(7, 32)
        )

        // Then
        assertThat(storedAlarm(alarmId))
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
        verifyScheduledForNextMonday(alarmId)
    }

    // Code scanned

    @Test
    fun `given ringing one-time alarm, when code is scanned, then it is finished, disabled and cancelled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(isAlarmEnabled = true, repeatingMode = Alarm.RepeatingMode.Once)
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.CodeScanned,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given snoozed repeating alarm, when code is scanned, then it is un-snoozed and set for its next occurrence`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        dismissAlarm(alarmId, DismissAlarm.Reason.CodeScanned, now = SATURDAY.atTime(7, 32))

        // Then
        assertThat(storedAlarm(alarmId))
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
        verifyScheduledForNextMonday(alarmId)
    }

    // Code scanned before the alarm fired

    @Test
    fun `given upcoming repeating alarm, when code is scanned before it fired, then that occurrence is skipped and the next one is set`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.CodeScannedBeforeAlarmFired,
            now = SATURDAY.atTime(7, 10)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        verifyScheduledForNextMonday(alarmId)
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isTrue()
    }

    @Test
    fun `given upcoming one-time alarm, when code is scanned before it fired, then it is disabled and cancelled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        dismissAlarm(
            alarmId,
            DismissAlarm.Reason.CodeScannedBeforeAlarmFired,
            now = SATURDAY.atTime(7, 10)
        )

        // Then
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    // Emergency task completed

    @Test
    fun `given one-time alarm, when the emergency task is completed, then it is finished, disabled and cancelled`() = runTest {
        // Given
        givenEmergencyDisablesRepeatingAlarms(enabled = true)
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(isAlarmEnabled = true, repeatingMode = Alarm.RepeatingMode.Once)
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.EmergencyTaskCompleted,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given repeating alarm and emergency set to disable repeating alarms, when the emergency task is completed, then it is disabled and reported as such`() = runTest {
        // Given
        givenEmergencyDisablesRepeatingAlarms(enabled = true)
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(isAlarmEnabled = true, repeatingMode = MONDAYS_AND_SATURDAYS)
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.EmergencyTaskCompleted,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.RepeatingAlarmDisabled)
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given snoozed repeating alarm and emergency set to keep repeating alarms, when the emergency task is completed, then it is un-snoozed and set for its next occurrence`() = runTest {
        // Given
        givenEmergencyDisablesRepeatingAlarms(enabled = false)
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.EmergencyTaskCompleted,
            now = SATURDAY.atTime(7, 32)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        assertThat(storedAlarm(alarmId))
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
        verifyScheduledForNextMonday(alarmId)
    }

    @Test
    fun `given ringing repeating alarm that is not snoozed and emergency set to keep repeating alarms, when the emergency task is completed, then nothing is scheduled or cancelled`() = runTest {
        // Given
        givenEmergencyDisablesRepeatingAlarms(enabled = false)
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = false
            )
        )

        // When
        val result = dismissAlarm(
            alarmId,
            DismissAlarm.Reason.EmergencyTaskCompleted,
            now = SATURDAY.atTime(7, 30, 20)
        )

        // Then
        assertThat(result).isEqualTo(DismissAlarm.Result.Finished)
        verify { alarmScheduler wasNot Called }
    }

    private companion object {
        val SATURDAY: LocalDate = LocalDate.of(2026, 10, 17)
        val NEXT_MONDAY: LocalDate = LocalDate.of(2026, 10, 19)
        val MONDAYS_AND_SATURDAYS = Alarm.RepeatingMode.Days(
            repeatingDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)
        )
    }
}
