package com.sweak.qralarm.alarm.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.sweak.qralarm.alarm.service.AlarmDeliveryValidator.Verdict
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(TestParameterInjector::class)
class AlarmDeliveryValidatorTest {

    private val validator = AlarmDeliveryValidator(clock = fixedClock(NOW))

    enum class LaunchDelayCase(val launchDelayInMillis: Long, val expectedVerdict: Verdict) {
        ON_TIME(launchDelayInMillis = 0, expectedVerdict = Verdict.Ring(waitMillis = 0)),
        EARLY_WITHIN_TOLERANCE(
            launchDelayInMillis = -3_000,
            expectedVerdict = Verdict.Ring(waitMillis = 3_000)
        ),
        EARLY_AT_TOLERANCE(
            launchDelayInMillis = -5_000,
            expectedVerdict = Verdict.Ring(waitMillis = 5_000)
        ),
        EARLY_BEYOND_TOLERANCE(launchDelayInMillis = -5_001, expectedVerdict = Verdict.StopEarly),
        LATE_WITHIN_TOLERANCE(
            launchDelayInMillis = 9 * 60_000L,
            expectedVerdict = Verdict.Ring(waitMillis = 0)
        ),
        LATE_AT_TOLERANCE(
            launchDelayInMillis = 10 * 60_000L,
            expectedVerdict = Verdict.Ring(waitMillis = 0)
        ),
        LATE_BEYOND_TOLERANCE(
            launchDelayInMillis = 10 * 60_000L + 1,
            expectedVerdict = Verdict.StopEarly
        )
    }

    enum class RegularDeliveryFlag(val isSnoozeAlarm: Boolean?) {
        NOT_SNOOZE(isSnoozeAlarm = false),
        UNKNOWN(isSnoozeAlarm = null)
    }

    @Test
    fun `given no alarm, when validating, then the delivery is stopped`() {
        // When
        val verdict = validator.validate(alarm = null, isSnoozeAlarm = false)

        // Then
        assertThat(verdict).isEqualTo(Verdict.StopEarly)
    }

    @Test
    fun `given a regular delivery, when validating, then it rings only within 5 seconds early to 10 minutes late, waiting out an early delivery`(
        @TestParameter case: LaunchDelayCase,
        @TestParameter flag: RegularDeliveryFlag
    ) {
        // Given
        val alarm = alarm(
            nextAlarmTimeInMillis = NOW_MILLIS - case.launchDelayInMillis,
            nextSnoozedAlarmTimeInMillis = null
        )

        // When
        val verdict = validator.validate(alarm = alarm, isSnoozeAlarm = flag.isSnoozeAlarm)

        // Then
        assertThat(verdict).isEqualTo(case.expectedVerdict)
    }

    @Test
    fun `given a snooze delivery, when validating, then the snooze time is checked instead of the alarm time`() {
        // Given
        val alarm = alarm(
            nextAlarmTimeInMillis = NOW_MILLIS - ONE_DAY_MILLIS,
            isAlarmSnoozed = true,
            nextSnoozedAlarmTimeInMillis = NOW_MILLIS + 2_000
        )

        // When
        val verdict = validator.validate(alarm = alarm, isSnoozeAlarm = true)

        // Then
        assertThat(verdict).isEqualTo(Verdict.Ring(waitMillis = 2_000))
    }

    @Test
    fun `given a snooze delivery too far past its snooze time, when validating, then it is stopped even though the alarm time is now`() {
        // Given
        val alarm = alarm(
            nextAlarmTimeInMillis = NOW_MILLIS,
            isAlarmSnoozed = true,
            nextSnoozedAlarmTimeInMillis = NOW_MILLIS - 11 * 60_000L
        )

        // When
        val verdict = validator.validate(alarm = alarm, isSnoozeAlarm = true)

        // Then
        assertThat(verdict).isEqualTo(Verdict.StopEarly)
    }

    @Test
    fun `given a snooze delivery without a snooze time, when validating, then it rings without waiting`() {
        // Given
        val alarm = alarm(
            nextAlarmTimeInMillis = NOW_MILLIS - ONE_DAY_MILLIS,
            nextSnoozedAlarmTimeInMillis = null
        )

        // When
        val verdict = validator.validate(alarm = alarm, isSnoozeAlarm = true)

        // Then
        assertThat(verdict).isEqualTo(Verdict.Ring(waitMillis = 0))
    }

    private companion object {
        val NOW: LocalDateTime = LocalDateTime.of(2026, 11, 17, 7, 0)
        val NOW_MILLIS = epochMillisAt(NOW)
        const val ONE_DAY_MILLIS = 24 * 60 * 60_000L
    }
}
