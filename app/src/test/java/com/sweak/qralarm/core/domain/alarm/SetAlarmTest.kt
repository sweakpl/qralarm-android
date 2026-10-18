package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
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
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class SetAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)

    private suspend fun setAlarm(
        alarmId: Long,
        now: LocalDateTime,
        isReschedulingMissedAlarm: Boolean = false
    ) = SetAlarm(
        alarmScheduler = alarmScheduler,
        alarmNotifier = alarmNotifier,
        alarmsRepository = alarmsRepository,
        clock = fixedClock(now)
    )(alarmId = alarmId, isReschedulingMissedAlarm = isReschedulingMissedAlarm)

    @Test
    fun `given one-time alarm stored for today with its time still ahead, when setting, then it is set for today`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(6, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SATURDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given one-time alarm stored for today with its time already passed, when setting, then it is set for tomorrow`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SUNDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given one-time alarm stored for today with its time equal to now, when setting, then it is set for tomorrow`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(7, 30))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SUNDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given one-time alarm stored for a future date, when setting, then the stored date is kept`() = runTest {
        // Given
        val futureDate = SATURDAY.plusDays(4)
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(futureDate.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(futureDate.atTime(7, 30)))
        )
    }

    @Test
    fun `given one-time alarm stored for a date several days back, when setting, then it is set for the next occurrence from now`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.minusDays(5).atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(6, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SATURDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given today is Saturday and alarm repeating on Monday and Wednesday, when setting, then it is set for Monday`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Days(
                    listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
                )
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(MONDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given alarm repeating on today with its time still ahead, when setting, then it is set for today`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Days(
                    listOf(DayOfWeek.SATURDAY, DayOfWeek.MONDAY)
                )
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(6, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SATURDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given alarm repeating only on today with its time already passed, when setting, then it is set for the same weekday next week`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Days(listOf(DayOfWeek.SATURDAY))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(NEXT_SATURDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given repeating alarm skipped until its next occurrence, when setting, then the skipped occurrence is passed over`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Days(
                    listOf(DayOfWeek.SATURDAY, DayOfWeek.MONDAY)
                ),
                skipAlarmUntilTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SATURDAY.atTime(6, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(MONDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given repeating alarm whose next occurrence is after the spring-forward night, when setting, then it keeps its wall-clock time`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Days(listOf(DayOfWeek.SUNDAY))
            )
        )

        // When
        val result = setAlarm(alarmId, now = SPRING_FORWARD_SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(
                alarmTimInMillis = epochMillisAt(SPRING_FORWARD_SATURDAY.plusDays(1).atTime(7, 30))
            )
        )
    }

    @Test
    fun `given missed alarm being rescheduled, when setting, then it is set for today at its time even though that has passed`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        val result = setAlarm(
            alarmId,
            now = SATURDAY.atTime(7, 33),
            isReschedulingMissedAlarm = true
        )

        // Then
        assertThat(result).isEqualTo(
            SetAlarm.Result.Success(alarmTimInMillis = epochMillisAt(SATURDAY.atTime(7, 30)))
        )
    }

    @Test
    fun `given missed alarm being rescheduled, when setting, then no upcoming notification is scheduled or shown`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(7, 33), isReschedulingMissedAlarm = true)

        // Then
        verify(exactly = 0) { alarmScheduler.scheduleUpcomingAlarmNotification(any(), any()) }
        verify { alarmNotifier wasNot Called }
    }

    @Test
    fun `given alarm, when setting, then the computed time is stored as its next alarm time`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::nextAlarmTimeInMillis)
            .isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
    }

    @Test
    fun `given alarm, when setting, then a regular alarm is scheduled at the computed time`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(SUNDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    @Test
    fun `given alarm more than 2 h away, when setting, then the previous upcoming notification is cancelled before the new one is scheduled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(5, 0))

        // Then
        verifyOrder {
            alarmScheduler.cancelUpcomingAlarmNotification(alarmId = alarmId)
            alarmScheduler.scheduleUpcomingAlarmNotification(
                alarmId = alarmId,
                upcomingAlarmNotificationTimeInMillis = any()
            )
        }
    }

    @Test
    fun `given disabled alarm, when setting, then it is stored as enabled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(isAlarmEnabled = false))

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::isAlarmEnabled)
            .isTrue()
    }

    @Test
    fun `given alarm more than 2 h away, when setting, then the upcoming notification is scheduled for 2 h before and nothing is shown`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(5, 29))

        // Then
        verify {
            alarmScheduler.scheduleUpcomingAlarmNotification(
                alarmId = alarmId,
                upcomingAlarmNotificationTimeInMillis = epochMillisAt(SATURDAY.atTime(5, 30))
            )
        }
        verify { alarmNotifier wasNot Called }
    }

    @Test
    fun `given alarm exactly 2 h away, when setting, then the upcoming notification is shown now and nothing is scheduled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        setAlarm(alarmId, now = SATURDAY.atTime(5, 30))

        // Then
        verify {
            alarmNotifier.showUpcomingAlarmNotification(
                alarmId = alarmId,
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isSnoozeAlarm = false
            )
        }
        verify(exactly = 0) { alarmScheduler.scheduleUpcomingAlarmNotification(any(), any()) }
    }

    @Test
    fun `given no alarm with the id, when setting, then it fails and nothing is scheduled or shown`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        val result = setAlarm(missingAlarmId, now = SATURDAY.atTime(10, 0))

        // Then
        assertThat(result).isEqualTo(SetAlarm.Result.Failure)
        verify {
            alarmScheduler wasNot Called
            alarmNotifier wasNot Called
        }
    }

    private companion object {
        val SATURDAY: LocalDate = LocalDate.of(2026, 10, 17)
        val SUNDAY: LocalDate = SATURDAY.plusDays(1)
        val MONDAY: LocalDate = SATURDAY.plusDays(2)
        val NEXT_SATURDAY: LocalDate = SATURDAY.plusWeeks(1)
        val SPRING_FORWARD_SATURDAY: LocalDate = LocalDate.of(2026, 3, 28)
    }
}
