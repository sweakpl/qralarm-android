package com.sweak.qralarm.core.domain.alarm

import assertk.all
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
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SnoozeAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)

    private suspend fun snoozeAlarm(
        alarmId: Long,
        now: LocalDateTime,
        isReschedulingCurrentOrMissedSnooze: Boolean = false
    ) = SnoozeAlarm(
        alarmScheduler = alarmScheduler,
        alarmNotifier = alarmNotifier,
        alarmsRepository = alarmsRepository,
        clock = fixedClock(now)
    )(
        alarmId = alarmId,
        isReschedulingCurrentOrMissedSnooze = isReschedulingCurrentOrMissedSnooze
    )

    @Test
    fun `given no alarm with the id, when snoozing, then nothing is scheduled or shown`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        snoozeAlarm(missingAlarmId, now = TODAY.atTime(7, 30))

        // Then
        verify {
            alarmScheduler wasNot Called
            alarmNotifier wasNot Called
        }
    }

    @Test
    fun `given ringing alarm, when snoozing, then it is stored as snoozed until the current minute plus the snooze duration`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                snoozeDurationInMinutes = 5,
                isAlarmSnoozed = false,
                nextSnoozedAlarmTimeInMillis = null
            )
        )

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 30, 45))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .all {
                prop(Alarm.SnoozeConfig::isAlarmSnoozed).isTrue()
                prop(Alarm.SnoozeConfig::nextSnoozedAlarmTimeInMillis)
                    .isEqualTo(epochMillisAt(TODAY.atTime(7, 35)))
            }
    }

    @Test
    fun `given ringing alarm, when snoozing, then a snoozed alarm is scheduled at the current minute plus the snooze duration`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(snoozeDurationInMinutes = 5))

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 30, 45))

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 35)),
                isSnoozeAlarm = true
            )
        }
    }

    @Test
    fun `given ringing alarm, when snoozing, then a snoozed upcoming notification is shown with the snooze hour and minute`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(snoozeDurationInMinutes = 5))

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 30, 45))

        // Then
        verify {
            alarmNotifier.showUpcomingAlarmNotification(
                alarmId = alarmId,
                alarmHourOfDay = 7,
                alarmMinute = 35,
                isSnoozeAlarm = true
            )
        }
    }

    @Test
    fun `given alarm with snoozes left, when snoozing, then one snooze is used up`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(numberOfSnoozesLeft = 2))

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 30))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(1)
    }

    @Test
    fun `given alarm with no snoozes left, when snoozing, then the snooze count stays at zero`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(numberOfSnoozesLeft = 0))

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 30))

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(0)
    }

    @Test
    fun `given snoozed alarm being rescheduled, when snoozing, then it is scheduled at the stored snooze time`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                snoozeDurationInMinutes = 5,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 40))
            )
        )

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 32), isReschedulingCurrentOrMissedSnooze = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 40)),
                isSnoozeAlarm = true
            )
        }
    }

    @Test
    fun `given snoozed alarm being rescheduled, when snoozing, then no snooze is used up`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                numberOfSnoozesLeft = 2,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 40))
            )
        )

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 32), isReschedulingCurrentOrMissedSnooze = true)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(2)
    }

    @Test
    fun `given alarm with no stored snooze time being rescheduled, when snoozing, then nothing is scheduled or shown`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(isAlarmSnoozed = true, nextSnoozedAlarmTimeInMillis = null)
        )

        // When
        snoozeAlarm(alarmId, now = TODAY.atTime(7, 32), isReschedulingCurrentOrMissedSnooze = true)

        // Then
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
        verify { alarmNotifier wasNot Called }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 10, 17)
    }
}
