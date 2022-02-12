package com.sweak.qralarm.util

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.*

class CompatTimeUtilsTest {

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
            expectedAlarmMinute,
            TimeFormat.MILITARY,
            Meridiem.AM // not important here
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
            expectedAlarmMinute,
            TimeFormat.MILITARY,
            Meridiem.AM // not important here
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
            expectedAlarmMinute,
            TimeFormat.MILITARY,
            Meridiem.AM // not important here
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
            expectedAlarmMinute,
            TimeFormat.MILITARY,
            Meridiem.AM // not important here
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
            expectedAlarmMinute,
            TimeFormat.MILITARY,
            Meridiem.AM // not important here
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
    fun returnsCorrectAlarmTimeInMillisFor12_00AM() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val amPmAlarmHour = 12
        val alarmMeridiem = Meridiem.AM
        val expectedAlarmHour = 0
        val expectedAlarmMinute = 0

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            amPmAlarmHour,
            expectedAlarmMinute,
            TimeFormat.AMPM,
            alarmMeridiem
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
    fun returnsCorrectAlarmTimeInMillisFor7_30AM() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val amPmAlarmHour = 7
        val alarmMeridiem = Meridiem.AM
        val expectedAlarmHour = 7
        val expectedAlarmMinute = 30

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            amPmAlarmHour,
            expectedAlarmMinute,
            TimeFormat.AMPM,
            alarmMeridiem
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
    fun returnsCorrectAlarmTimeInMillisFor12_00PM() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val amPmAlarmHour = 12
        val alarmMeridiem = Meridiem.PM
        val expectedAlarmHour = 12
        val expectedAlarmMinute = 0

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            amPmAlarmHour,
            expectedAlarmMinute,
            TimeFormat.AMPM,
            alarmMeridiem
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
    fun returnsCorrectAlarmTimeInMillisFor4_15PM() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val amPmAlarmHour = 4
        val alarmMeridiem = Meridiem.PM
        val expectedAlarmHour = 16
        val expectedAlarmMinute = 15

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            amPmAlarmHour,
            expectedAlarmMinute,
            TimeFormat.AMPM,
            alarmMeridiem
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
    fun returnsCorrectAlarmTimeInMillisFor11_59PM() {
        // given
        val currentTimeInMillis = currentTimeInMillis()
        val amPmAlarmHour = 11
        val alarmMeridiem = Meridiem.PM
        val expectedAlarmHour = 23
        val expectedAlarmMinute = 59

        // when
        val alarmTimeInMillis = getAlarmTimeInMillis(
            amPmAlarmHour,
            expectedAlarmMinute,
            TimeFormat.AMPM,
            alarmMeridiem
        )
        val actualAlarmHour = getTestAlarmHour(alarmTimeInMillis)
        val actualAlarmMinute = getTestAlarmMinute(alarmTimeInMillis)

        // then
        assertTrue(alarmTimeInMillis > currentTimeInMillis)
        assertTrue(alarmTimeInMillis - currentTimeInMillis < 86400000)
        assertEquals(expectedAlarmHour, actualAlarmHour)
        assertEquals(expectedAlarmMinute, actualAlarmMinute)
    }

    // Below tests are ran under timezone: UTC+1h

    @Test
    fun returnCorrectAlarmHourMinuteAndMeridiemFor00_00() {
        // given
        val timeInMillis = 1644620400000
        val expectedHour = 0
        val expectedMinute = 0
        val expectedMeridiem = Meridiem.AM

        // when
        val actualHour = getAlarmHour(timeInMillis, TimeFormat.MILITARY)
        val actualMinute = getAlarmMinute(timeInMillis)
        val actualMeridiem = getAlarmMeridiem(timeInMillis)

        // then
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedMeridiem, actualMeridiem)
    }

    @Test
    fun returnCorrectAlarmHourMinuteAndMeridiemFor06_50() {
        // given
        val timeInMillis = 1644645000000
        val expectedHour = 6
        val expectedMinute = 50
        val expectedMeridiem = Meridiem.AM

        // when
        val actualHour = getAlarmHour(timeInMillis, TimeFormat.MILITARY)
        val actualMinute = getAlarmMinute(timeInMillis)
        val actualMeridiem = getAlarmMeridiem(timeInMillis)

        // then
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedMeridiem, actualMeridiem)
    }

    @Test
    fun returnCorrectAlarmHourMinuteAndMeridiemFor12_00() {
        // given
        val timeInMillis = 1644663600000
        val expectedHour = 12
        val expectedMinute = 0
        val expectedMeridiem = Meridiem.PM

        // when
        val actualHour = getAlarmHour(timeInMillis, TimeFormat.MILITARY)
        val actualMinute = getAlarmMinute(timeInMillis)
        val actualMeridiem = getAlarmMeridiem(timeInMillis)

        // then
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedMeridiem, actualMeridiem)
    }

    @Test
    fun returnCorrectAlarmHourMinuteAndMeridiemFor17_10() {
        // given
        val timeInMillis = 1644682200000
        val expectedHour = 17
        val expectedMinute = 10
        val expectedMeridiem = Meridiem.PM

        // when
        val actualHour = getAlarmHour(timeInMillis, TimeFormat.MILITARY)
        val actualMinute = getAlarmMinute(timeInMillis)
        val actualMeridiem = getAlarmMeridiem(timeInMillis)

        // then
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedMeridiem, actualMeridiem)
    }

    @Test
    fun returnCorrectAlarmHourMinuteAndMeridiemFor23_59() {
        // given
        val timeInMillis = 1644706740000
        val expectedHour = 23
        val expectedMinute = 59
        val expectedMeridiem = Meridiem.PM

        // when
        val actualHour = getAlarmHour(timeInMillis, TimeFormat.MILITARY)
        val actualMinute = getAlarmMinute(timeInMillis)
        val actualMeridiem = getAlarmMeridiem(timeInMillis)

        // then
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedMeridiem, actualMeridiem)
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