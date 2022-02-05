package com.sweak.qralarm.util

import org.junit.Assert.*
import org.junit.Test

class SwapTimeFormatsTest {

    @Test
    fun `Parses from 0 __ in MILITARY to 12 __ AM in AMPM`() {
        // given
        val hour = 0
        val timeFormatString = TimeFormat.MILITARY.name

        // when
        val expectedNewHour = 12
        val expectedNewTimeFormatString = TimeFormat.AMPM.name
        val expectedNewMeridiemString = Meridiem.AM.name

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, false, hour)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 9 __ in MILITARY to 9 __ AM in AMPM`() {
        // given
        val hour = 9
        val timeFormatString = TimeFormat.MILITARY.name

        // when
        val expectedNewHour = 9
        val expectedNewTimeFormatString = TimeFormat.AMPM.name
        val expectedNewMeridiemString = Meridiem.AM.name

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, false, hour)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 12 __ in MILITARY to 12 __ PM in AMPM`() {
        // given
        val hour = 12
        val timeFormatString = TimeFormat.MILITARY.name

        // when
        val expectedNewHour = 12
        val expectedNewTimeFormatString = TimeFormat.AMPM.name
        val expectedNewMeridiemString = Meridiem.PM.name

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, false, hour)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 17 __ in MILITARY to 5 __ PM in AMPM`() {
        // given
        val hour = 17
        val timeFormatString = TimeFormat.MILITARY.name

        // when
        val expectedNewHour = 5
        val expectedNewTimeFormatString = TimeFormat.AMPM.name
        val expectedNewMeridiemString = Meridiem.PM.name

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, false, hour)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 12 __ AM in AMPM to 0 __ in MILITARY`() {
        // given
        val hour = 12
        val timeFormatString = TimeFormat.AMPM.name
        val meridiemString = Meridiem.AM.name

        // when
        val expectedNewHour = 0
        val expectedNewTimeFormatString = TimeFormat.MILITARY.name
        val expectedNewMeridiemString = null

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, true, hour, meridiemString)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 7 __ AM in AMPM to 7 __ in MILITARY`() {
        // given
        val hour = 7
        val timeFormatString = TimeFormat.AMPM.name
        val meridiemString = Meridiem.AM.name

        // when
        val expectedNewHour = 7
        val expectedNewTimeFormatString = TimeFormat.MILITARY.name
        val expectedNewMeridiemString = null

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, true, hour, meridiemString)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 12 __ PM in AMPM to 12 __ in MILITARY`() {
        // given
        val hour = 12
        val timeFormatString = TimeFormat.AMPM.name
        val meridiemString = Meridiem.PM.name

        // when
        val expectedNewHour = 12
        val expectedNewTimeFormatString = TimeFormat.MILITARY.name
        val expectedNewMeridiemString = null

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, true, hour, meridiemString)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }

    @Test
    fun `Parses from 9 __ PM in AMPM to 21 __ in MILITARY`() {
        // given
        val hour = 9
        val timeFormatString = TimeFormat.AMPM.name
        val meridiemString = Meridiem.PM.name

        // when
        val expectedNewHour = 21
        val expectedNewTimeFormatString = TimeFormat.MILITARY.name
        val expectedNewMeridiemString = null

        val (newHour, newTimeFormatString, newMeridiemString) =
            swapTimeFormats(timeFormatString, true, hour, meridiemString)

        // then
        assertEquals(expectedNewHour, newHour)
        assertEquals(expectedNewTimeFormatString, newTimeFormatString)
        assertEquals(expectedNewMeridiemString, newMeridiemString)
    }
}