package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.domain.user.UserDataRepository
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class RescheduleAlarmsTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmNotifier = mockk<AlarmNotifier>(relaxUnitFun = true)
    private val userDataRepository = mockk<UserDataRepository>(relaxUnitFun = true)

    private suspend fun rescheduleAlarms(
        now: LocalDateTime,
        rescheduleAlarmsIfMissedByFiveMinutes: Boolean
    ) {
        val clock = fixedClock(now)

        RescheduleAlarms(
            alarmsRepository = alarmsRepository,
            alarmScheduler = alarmScheduler,
            alarmNotifier = alarmNotifier,
            userDataRepository = userDataRepository,
            setAlarm = SetAlarm(
                alarmScheduler = alarmScheduler,
                alarmNotifier = alarmNotifier,
                alarmsRepository = alarmsRepository,
                clock = clock
            ),
            disableAlarm = DisableAlarm(
                alarmScheduler = alarmScheduler,
                alarmsRepository = alarmsRepository
            ),
            snoozeAlarm = SnoozeAlarm(
                alarmScheduler = alarmScheduler,
                alarmNotifier = alarmNotifier,
                alarmsRepository = alarmsRepository,
                clock = clock
            ),
            clock = clock
        )(rescheduleAlarmsIfMissedByFiveMinutes = rescheduleAlarmsIfMissedByFiveMinutes)
    }

    private fun verifyReportedAsMissed() {
        coVerify {
            alarmNotifier.notifyAboutMissedAlarm()
            userDataRepository.setAlarmMissedDetected(detected = true)
        }
    }

    private fun verifyNotReportedAsMissed() {
        verify(exactly = 0) { alarmNotifier.notifyAboutMissedAlarm() }
        verify { userDataRepository wasNot Called }
    }

    @Test
    fun `given no exact alarm permission, when rescheduling, then every alarm is disabled`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns false
        alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )
        alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 32), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        assertThat(alarmsRepository.getAllAlarms().first().map { it.isAlarmEnabled })
            .containsExactly(false, false)
    }

    @Test
    fun `given no exact alarm permission, when rescheduling, then every alarm is cancelled and nothing is scheduled`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns false
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )
        val snoozedAlarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 32), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.cancelAlarm(alarmId = alarmId)
            alarmScheduler.cancelAlarm(alarmId = snoozedAlarmId)
        }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    @Test
    fun `given enabled alarm in the future, when rescheduling, then it is scheduled at its next time`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
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
        rescheduleAlarms(now = SATURDAY.atTime(6, 0), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    @Test
    fun `given alarm missed by less than five minutes, when rescheduling, then it is re-armed for today's time`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
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
        rescheduleAlarms(now = SATURDAY.atTime(7, 33), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    @Test
    fun `given alarm missed by less than five minutes, when rescheduling, then it is not reported as missed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 33), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verifyNotReportedAsMissed()
    }

    @Test
    fun `given alarm missed by less than five minutes and no rescheduling of barely missed alarms, when rescheduling, then it is reported as missed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 33), rescheduleAlarmsIfMissedByFiveMinutes = false)

        // Then
        verifyReportedAsMissed()
    }

    @Test
    fun `given alarm missed by exactly five minutes, when rescheduling, then it is reported as missed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 35), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verifyReportedAsMissed()
    }

    @Test
    fun `given one-time alarm missed by more than five minutes, when rescheduling, then it is disabled and cancelled`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
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
        rescheduleAlarms(now = SATURDAY.atTime(7, 45), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::isAlarmEnabled)
            .isFalse()
        verify { alarmScheduler.cancelAlarm(alarmId = alarmId) }
        verify(exactly = 0) { alarmScheduler.setAlarm(any(), any(), any()) }
    }

    @Test
    fun `given repeating alarm missed by more than five minutes, when rescheduling, then it is scheduled for its next occurrence`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
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
        rescheduleAlarms(now = SATURDAY.atTime(7, 45), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(NEXT_MONDAY.atTime(7, 30)),
                isSnoozeAlarm = false
            )
        }
    }

    @Test
    fun `given disabled alarm with a past time, when rescheduling, then it is left untouched`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                nextAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 30)),
                isAlarmSnoozed = false
            )
        )
        val storedAlarm = alarmsRepository.getAlarm(alarmId)

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 45), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId)).isEqualTo(storedAlarm)
        verify(exactly = 0) {
            alarmScheduler.setAlarm(any(), any(), any())
            alarmScheduler.cancelAlarm(any())
        }
        verifyNotReportedAsMissed()
    }

    @Test
    fun `given snooze in the future, when rescheduling, then a snoozed alarm is scheduled at the stored snooze time`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                snoozeDurationInMinutes = 5,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 32), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40)),
                isSnoozeAlarm = true
            )
        }
    }

    @Test
    fun `given snooze in the future, when rescheduling, then no snooze is used up`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                numberOfSnoozesLeft = 2,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 32), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::numberOfSnoozesLeft)
            .isEqualTo(2)
    }

    @Test
    fun `given snooze missed by less than five minutes, when rescheduling, then it is re-armed at the stored snooze time`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                snoozeDurationInMinutes = 5,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 43), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verify {
            alarmScheduler.setAlarm(
                alarmId = alarmId,
                alarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40)),
                isSnoozeAlarm = true
            )
        }
    }

    @Test
    fun `given snooze missed by less than five minutes and no rescheduling of barely missed alarms, when rescheduling, then it is reported as missed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = false,
                repeatingMode = Alarm.RepeatingMode.Once,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 43), rescheduleAlarmsIfMissedByFiveMinutes = false)

        // Then
        verifyReportedAsMissed()
    }

    @Test
    fun `given snooze missed by more than five minutes, when rescheduling, then it is reported as missed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 50), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        verifyReportedAsMissed()
    }

    @Test
    fun `given repeating alarm with snooze missed by more than five minutes, when rescheduling, then it is no longer snoozed`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 50), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
    }

    @Test
    fun `given repeating alarm with snooze missed by more than five minutes, when rescheduling, then it is scheduled for its next occurrence`() = runTest {
        // Given
        every { alarmScheduler.canScheduleExactAlarms() } returns true
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                alarmHourOfDay = 7,
                alarmMinute = 30,
                isAlarmEnabled = true,
                repeatingMode = MONDAYS_AND_SATURDAYS,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = epochMillisAt(SATURDAY.atTime(7, 40))
            )
        )

        // When
        rescheduleAlarms(now = SATURDAY.atTime(7, 50), rescheduleAlarmsIfMissedByFiveMinutes = true)

        // Then
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
