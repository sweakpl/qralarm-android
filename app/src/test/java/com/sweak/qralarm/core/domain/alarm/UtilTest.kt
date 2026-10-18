package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class UtilTest {

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
}
