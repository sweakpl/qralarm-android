package com.sweak.qralarm.features.add_edit_alarm.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class AlarmScheduleCalculatorTest {

    private fun calculator(now: LocalDateTime) = AlarmScheduleCalculator(clock = fixedClock(now))

    @Test
    fun `given one-time alarm dated today with its time still ahead, when computing next alarm time, then it is today`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(6, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE),
            onlyOnceAlarmDateInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SATURDAY.atTime(7, 30)))
    }

    @Test
    fun `given one-time alarm dated today with its time already passed, when computing next alarm time, then it is tomorrow`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE),
            onlyOnceAlarmDateInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
    }

    @Test
    fun `given one-time alarm dated a few days ahead, when computing next alarm time, then it is on that date at the alarm time`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE),
            onlyOnceAlarmDateInMillis = epochMillisAt(WEDNESDAY.atTime(9, 15))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(WEDNESDAY.atTime(7, 30)))
    }

    @Test
    fun `given Monday to Friday alarm on Saturday, when computing next alarm time, then it is Monday`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(6, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI),
            onlyOnceAlarmDateInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(MONDAY.atTime(7, 30)))
    }

    @Test
    fun `given weekend alarm on Saturday with its time still ahead, when computing next alarm time, then it is today`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(6, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = SAT_SUN),
            onlyOnceAlarmDateInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SATURDAY.atTime(7, 30)))
    }

    @Test
    fun `given everyday alarm with its time already passed, when computing next alarm time, then it is tomorrow`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = EVERYDAY),
            onlyOnceAlarmDateInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
    }

    @Test
    fun `given custom days alarm with today's occurrence already passed, when computing next alarm time, then it is the next chosen day`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.computeNextAlarmTimeInMillis(
            hourOfDay = 7,
            minute = 30,
            repeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(
                alarmRepeatingMode = CUSTOM,
                alarmDaysOfWeek = listOf(DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)
            ),
            onlyOnceAlarmDateInMillis = epochMillisAt(SATURDAY.atTime(7, 30))
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(WEDNESDAY.atTime(7, 30)))
    }

    @Test
    fun `given sticky date and alarm time still ahead, when resolving one-time alarm date, then it is today`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(6, 0))

        // When
        val result = calculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30)),
            hourOfDay = 7,
            minute = 30,
            isDateSticky = true
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SATURDAY.atTime(7, 30)))
    }

    @Test
    fun `given sticky date and alarm time already passed, when resolving one-time alarm date, then it is tomorrow`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = epochMillisAt(WEDNESDAY.atTime(7, 30)),
            hourOfDay = 7,
            minute = 30,
            isDateSticky = true
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
    }

    @Test
    fun `given non-sticky date ahead, when resolving one-time alarm date, then the date is kept with the new alarm time`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = epochMillisAt(WEDNESDAY.atTime(9, 15)),
            hourOfDay = 7,
            minute = 30,
            isDateSticky = false
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(WEDNESDAY.atTime(7, 30)))
    }

    @Test
    fun `given non-sticky date already in the past, when resolving one-time alarm date, then it moves to the earliest possible date`() {
        // Given
        val calculator = calculator(now = SATURDAY.atTime(10, 0))

        // When
        val result = calculator.resolveOnlyOnceAlarmDateInMillis(
            currentDateInMillis = epochMillisAt(FRIDAY.atTime(7, 30)),
            hourOfDay = 7,
            minute = 30,
            isDateSticky = false
        )

        // Then
        assertThat(result).isEqualTo(epochMillisAt(SUNDAY.atTime(7, 30)))
    }

    @Test
    fun `given only once schedule, when converting to repeating mode, then it is a one-time mode`() {
        // Given
        val wrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = ONLY_ONCE)

        // When
        val result = wrapper.toRepeatingMode()

        // Then
        assertThat(result).isEqualTo(Alarm.RepeatingMode.Once)
    }

    @Test
    fun `given Monday to Friday schedule, when converting to repeating mode, then it repeats on the five working days`() {
        // Given
        val wrapper = AlarmRepeatingScheduleWrapper(alarmRepeatingMode = MON_FRI)

        // When
        val result = wrapper.toRepeatingMode()

        // Then
        assertThat(result).isEqualTo(
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
    }

    @Test
    fun `given custom schedule, when converting to repeating mode, then it repeats on the chosen days`() {
        // Given
        val wrapper = AlarmRepeatingScheduleWrapper(
            alarmRepeatingMode = CUSTOM,
            alarmDaysOfWeek = listOf(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
        )

        // When
        val result = wrapper.toRepeatingMode()

        // Then
        assertThat(result).isEqualTo(
            Alarm.RepeatingMode.Days(
                repeatingDaysOfWeek = listOf(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
            )
        )
    }

    private companion object {
        val FRIDAY: LocalDate = LocalDate.of(2026, 10, 16)
        val SATURDAY: LocalDate = LocalDate.of(2026, 10, 17)
        val SUNDAY: LocalDate = LocalDate.of(2026, 10, 18)
        val MONDAY: LocalDate = LocalDate.of(2026, 10, 19)
        val WEDNESDAY: LocalDate = LocalDate.of(2026, 10, 21)
    }
}
