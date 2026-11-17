package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isFalse
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

class HandleEarlyStoppedAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)

    private suspend fun handleEarlyStoppedAlarm(alarmId: Long, now: LocalDateTime) {
        val disableAlarm = DisableAlarm(
            alarmScheduler = alarmScheduler,
            alarmsRepository = alarmsRepository
        )

        HandleEarlyStoppedAlarm(
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
        )(alarmId = alarmId)
    }

    private suspend fun storedAlarm(alarmId: Long): Alarm = alarmsRepository.getAlarm(alarmId)!!

    @Test
    fun `given no alarm with the id, when handling the early stop, then nothing is scheduled`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        handleEarlyStoppedAlarm(missingAlarmId, now = SATURDAY.atTime(7, 30, 5))

        // Then
        verify { alarmScheduler wasNot Called }
    }

    @Test
    fun `given one-time alarm, when handling the early stop, then it is disabled and cancelled`() = runTest {
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
        handleEarlyStoppedAlarm(alarmId, now = SATURDAY.atTime(9, 0))

        // Then
        assertThat(storedAlarm(alarmId)).prop(Alarm::isAlarmEnabled).isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    @Test
    fun `given snoozed repeating alarm, when handling the early stop, then it is un-snoozed and set for its next occurrence`() = runTest {
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
        handleEarlyStoppedAlarm(alarmId, now = SATURDAY.atTime(9, 0))

        // Then
        assertThat(storedAlarm(alarmId))
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(NEXT_MONDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    private companion object {
        val SATURDAY: LocalDate = LocalDate.of(2026, 10, 17)
        val NEXT_MONDAY: LocalDate = LocalDate.of(2026, 10, 19)
        val MONDAYS_AND_SATURDAYS = Alarm.RepeatingMode.Days(
            repeatingDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)
        )
    }
}
