package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
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

class AdvanceAlarmToNextOccurrenceTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)

    private suspend fun advanceAlarmToNextOccurrence(alarmId: Long, now: LocalDateTime) =
        AdvanceAlarmToNextOccurrence(
            alarmsRepository = alarmsRepository,
            setAlarm = SetAlarm(
                alarmScheduler = alarmScheduler,
                alarmNotifier = alarmNotifier,
                alarmsRepository = alarmsRepository,
                clock = fixedClock(now)
            ),
            disableAlarm = DisableAlarm(
                alarmScheduler = alarmScheduler,
                alarmsRepository = alarmsRepository
            )
        )(alarmId = alarmId)

    @Test
    fun `given one-time alarm, when advancing the alarm to its next occurrence, then it is disabled and cancelled`() = runTest {
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
        advanceAlarmToNextOccurrence(alarmId, now = SATURDAY.atTime(7, 31))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::isAlarmEnabled)
            .isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    @Test
    fun `given repeating alarm, when advancing the alarm to its next occurrence, then it is set for its next occurrence`() = runTest {
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
        advanceAlarmToNextOccurrence(alarmId, now = SATURDAY.atTime(7, 31))

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(NEXT_MONDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::isAlarmEnabled)
            .isTrue()
    }

    @Test
    fun `given no alarm with the id, when advancing the alarm to its next occurrence, then nothing is scheduled or cancelled`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        advanceAlarmToNextOccurrence(missingAlarmId, now = SATURDAY.atTime(7, 31))

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
