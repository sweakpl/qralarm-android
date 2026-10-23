package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class UtilTest {

    @Test
    fun `given unsnoozed alarm with snoozes left, when checking snooze availability, then it is available`() {
        // Given
        val alarm = alarm(numberOfSnoozesLeft = 1, isAlarmSnoozed = false)

        // When
        val result = alarm.isSnoozeAvailable()

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `given alarm with no snoozes left, when checking snooze availability, then it is not available`() {
        // Given
        val alarm = alarm(numberOfSnoozesLeft = 0, isAlarmSnoozed = false)

        // When
        val result = alarm.isSnoozeAvailable()

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `given snoozed alarm with snoozes left, when checking snooze availability, then it is not available`() {
        // Given
        val alarm = alarm(
            numberOfSnoozesLeft = 2,
            isAlarmSnoozed = true,
            nextSnoozedAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 40))
        )

        // When
        val result = alarm.isSnoozeAvailable()

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `given alarm time, when getting its hour and minute in Warsaw, then they are Warsaw's wall-clock time`() {
        // Given
        val alarmTimeInMillis = Instant.parse("2026-10-17T05:35:00Z").toEpochMilli()

        // When
        val result = getHourAndMinuteOfAlarmTimeInMillis(
            alarmTimeInMillis = alarmTimeInMillis,
            zoneId = ZoneId.of("Europe/Warsaw")
        )

        // Then
        assertThat(result).isEqualTo(7 to 35)
    }

    @Test
    fun `given the same alarm time, when getting its hour and minute in Kolkata, then they are Kolkata's wall-clock time`() {
        // Given
        val alarmTimeInMillis = Instant.parse("2026-10-17T05:35:00Z").toEpochMilli()

        // When
        val result = getHourAndMinuteOfAlarmTimeInMillis(
            alarmTimeInMillis = alarmTimeInMillis,
            zoneId = ZoneId.of("Asia/Kolkata")
        )

        // Then
        assertThat(result).isEqualTo(11 to 5)
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 10, 17)
    }
}
