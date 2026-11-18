package com.sweak.qralarm.core.domain.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class RingAgainLaterTimeTest {
    private val zone = ZoneId.systemDefault()
    private fun at(hour: Int, minute: Int, second: Int = 0) =
        ZonedDateTime.of(2026, 11, 18, hour, minute, second, 0, zone).toInstant().toEpochMilli()

    @Test
    fun afterMinutesDropsSeconds() {
        assertEquals(at(7, 45), RingAgainLater.afterMinutes(15, nowMillis = at(7, 30, 42)))
    }

    @Test
    fun laterTodayStaysToday() {
        assertEquals(at(9, 0), RingAgainLater.nextOccurrence(9, 0, nowMillis = at(7, 30)))
    }

    @Test
    fun earlierTimeMeansTomorrow() {
        val next = RingAgainLater.nextOccurrence(6, 0, nowMillis = at(7, 30))
        assertEquals(ZonedDateTime.of(2026, 11, 19, 6, 0, 0, 0, zone).toInstant().toEpochMilli(), next)
    }

    @Test
    fun theCurrentMinuteMeansTomorrow() {
        assertTrue(RingAgainLater.nextOccurrence(7, 30, nowMillis = at(7, 30, 10)) > at(23, 59))
    }
}
