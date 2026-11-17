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
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class StartAlarmRingingTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)

    private suspend fun startAlarmRinging(
        alarmId: Long,
        isSnoozeAlarm: Boolean?,
        now: LocalDateTime
    ) {
        val disableAlarm = DisableAlarm(
            alarmScheduler = alarmScheduler,
            alarmsRepository = alarmsRepository
        )

        StartAlarmRinging(
            alarmsRepository = alarmsRepository,
            advanceAlarmToNextOccurrence = AdvanceAlarmToNextOccurrence(
                alarmsRepository = alarmsRepository,
                setAlarm = SetAlarm(
                    alarmScheduler = alarmScheduler,
                    alarmNotifier = alarmNotifier,
                    alarmsRepository = alarmsRepository,
                    clock = fixedClock(now)
                ),
                disableAlarm = disableAlarm
            )
        )(alarmId = alarmId, isSnoozeAlarm = isSnoozeAlarm)
    }

    private suspend fun storedAlarm(alarmId: Long): Alarm = alarmsRepository.getAlarm(alarmId)!!

    private suspend fun storedSnoozeConfig(alarmId: Long): Alarm.SnoozeConfig =
        storedAlarm(alarmId).snoozeConfig

    private fun verifyScheduledForNextMonday(alarmId: Long) {
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(NEXT_MONDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    @Test
    fun `given regular delivery of a repeating alarm, when ringing starts, then it is running, its snoozes are reset and it is set for its next occurrence`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmRunning = false,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                numberOfSnoozes = 3,
                numberOfSnoozesLeft = 1
            )
        )

        // When
        startAlarmRinging(alarmId, isSnoozeAlarm = false, now = SATURDAY.atTime(7, 30, 5))

        // Then
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmRunning).isTrue()
        assertThat(storedSnoozeConfig(alarmId))
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(3)
        verifyScheduledForNextMonday(alarmId)
    }

    @Test
    fun `given snoozed delivery, when ringing starts, then it is un-snoozed and its remaining snoozes are kept`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                numberOfSnoozes = 3,
                numberOfSnoozesLeft = 1,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        startAlarmRinging(alarmId, isSnoozeAlarm = true, now = SATURDAY.atTime(7, 40, 5))

        // Then
        assertThat(storedSnoozeConfig(alarmId))
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
        assertThat(storedSnoozeConfig(alarmId))
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(1)
        verifyScheduledForNextMonday(alarmId)
    }

    @Test
    fun `given delivery without the snooze extra, when ringing starts, then its remaining snoozes are kept`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                numberOfSnoozes = 3,
                numberOfSnoozesLeft = 1
            )
        )

        // When
        startAlarmRinging(alarmId, isSnoozeAlarm = null, now = SATURDAY.atTime(7, 30, 5))

        // Then
        assertThat(storedSnoozeConfig(alarmId))
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(1)
    }

    @Test
    fun `given regular delivery of a one-time alarm, when ringing starts, then it is disabled and cancelled`() = runTest {
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
        startAlarmRinging(alarmId, isSnoozeAlarm = false, now = SATURDAY.atTime(7, 30, 5))

        // Then
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    @Test
    fun `given no alarm with the id, when ringing starts, then nothing is scheduled`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        startAlarmRinging(missingAlarmId, isSnoozeAlarm = false, now = SATURDAY.atTime(7, 30, 5))

        // Then
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
