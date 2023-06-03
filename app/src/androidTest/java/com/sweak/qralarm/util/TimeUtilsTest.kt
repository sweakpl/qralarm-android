package com.sweak.qralarm.util

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.*

class TimeUtilsTest {

    @Test
    fun returnsTimeInMillisThatIsGreaterThanTimeInMillisOfCreatingThisTest() {
        // given
        val testCreationTimeInMillis = 1644688800000

        // when
        val currentTimeInMillis = currentTimeInMillis()

        // then
        assertTrue(currentTimeInMillis > testCreationTimeInMillis)
    }

    @Test
    fun returnsCorrectAlarmTimeInMillisFor00_00() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val expectedAlarmHour = 0
        val expectedAlarmMinute = 0

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            expectedAlarmHour,
            expectedAlarmMinute
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    @Test
    fun returnsCorrectAlarmTimeInMillisFor09_20() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val expectedAlarmHour = 9
        val expectedAlarmMinute = 20

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            expectedAlarmHour,
            expectedAlarmMinute
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    @Test
    fun returnsCorrectAlarmTimeInMillisFor12_00() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val expectedAlarmHour = 12
        val expectedAlarmMinute = 0

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            expectedAlarmHour,
            expectedAlarmMinute
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    @Test
    fun returnsCorrectAlarmTimeInMillisFor14_45() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val expectedAlarmHour = 14
        val expectedAlarmMinute = 45

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            expectedAlarmHour,
            expectedAlarmMinute
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    @Test
    fun returnsCorrectAlarmTimeInMillisFor23_59() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val expectedAlarmHour = 23
        val expectedAlarmMinute = 59

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            expectedAlarmHour,
            expectedAlarmMinute
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    @Test
    fun returnsCorrectTwoMinuteSnoozeAlarmTimeInMillis() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val executionTimeOffsetInMillis = 10
        val snoozeTimeInMinutes = 2

        // when
        val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(snoozeTimeInMinutes)

        // then
        assertTrue(snoozeAlarmTimeInMillis > currentTimeInMillis)
        assertTrue(
            snoozeAlarmTimeInMillis - currentTimeInMillis <
                    snoozeAlarmTimeInMillis * 60 * 1000 + executionTimeOffsetInMillis
        )
    }

    @Test
    fun returnsCorrectFiveMinuteSnoozeAlarmTimeInMillis() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val executionTimeOffsetInMillis = 10
        val snoozeTimeInMinutes = 5

        // when
        val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(snoozeTimeInMinutes)

        // then
        assertTrue(snoozeAlarmTimeInMillis > currentTimeInMillis)
        assertTrue(
            snoozeAlarmTimeInMillis - currentTimeInMillis <
                    snoozeAlarmTimeInMillis * 60 * 1000 + executionTimeOffsetInMillis
        )
    }

    @Test
    fun returnsCorrectTenMinuteSnoozeAlarmTimeInMillis() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val executionTimeOffsetInMillis = 10
        val snoozeTimeInMinutes = 10

        // when
        val snoozeAlarmTimeInMillis = getSnoozeAlarmTimeInMillis(snoozeTimeInMinutes)

        // then
        assertTrue(snoozeAlarmTimeInMillis > currentTimeInMillis)
        assertTrue(
            snoozeAlarmTimeInMillis - currentTimeInMillis <
                    snoozeAlarmTimeInMillis * 60 * 1000 + executionTimeOffsetInMillis
        )
    }

    @Test
    fun returns0HoursAnd1MinuteForAlarmStartingInLessThan1Minute() {
        // given
        val currentTime = 1672486170000
        val alarmTime = 1672486200000
        val expectedHours = 0
        val expectedMinutes = 1

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns0HoursAnd1MinuteForAlarmStartingInBetween2MinutesAnd1Minute() {
        // given
        val currentTime = 1672486110000
        val alarmTime = 1672486200000
        val expectedHours = 0
        val expectedMinutes = 1

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns0HoursAnd59MinutesForAlarmStartingInBetween59MinutesAnd1Hour() {
        // given
        val currentTime = 1672486230000
        val alarmTime = 1672489800000
        val expectedHours = 0
        val expectedMinutes = 59

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns1HourAnd0MinutesForAlarmStartingInBetween1HourAnd1HourAnd1Minute() {
        // given
        val currentTime = 1672486170000
        val alarmTime = 1672489800000
        val expectedHours = 1
        val expectedMinutes = 0

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns7HoursAnd43MinutesForAlarmStartingInBetween7HoursAnd43MinutesAnd7HoursAnd44Minutes() {
        // given
        val currentTime = 1672438590000
        val alarmTime = 1672466400000
        val expectedHours = 7
        val expectedMinutes = 43

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns12HoursAnd17MinutesForAlarmStartingInBetween12HoursAnd17MinutesAnd12HoursAnd18Minutes() {
        // given
        val currentTime = 1672429350000
        val alarmTime = 1672473600000
        val expectedHours = 12
        val expectedMinutes = 17

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun returns23HoursAnd59MinutesForAlarmStartingInNotMoreThan1MinuteLessThan24Hours() {
        // given
        val currentTime = 1672408830000
        val alarmTime = 1672495200000
        val expectedHours = 23
        val expectedMinutes = 59

        // when
        val hoursAndMinutesPair = getHoursAndMinutesUntilTimePairFromTime(currentTime, alarmTime)
        val actualHours = hoursAndMinutesPair.first
        val actualMinutes = hoursAndMinutesPair.second

        // then
        assertEquals(expectedHours, actualHours)
        assertEquals(expectedMinutes, actualMinutes)
    }

    private fun getTestAlarmMinute(alarmTimeInMillis: Long): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).minute
        } else {
            Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = alarmTimeInMillis
            }.get(Calendar.MINUTE)
        }

    private fun getTestAlarmHour(alarmTimeInMillis: Long): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.ofEpochMilli(alarmTimeInMillis).atZone(ZoneId.systemDefault()).hour
        } else {
            Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = alarmTimeInMillis
            }.get(Calendar.HOUR_OF_DAY)
        }
}