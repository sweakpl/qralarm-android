package com.sweak.qralarm.alarm.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class AlarmServiceRingingStateTest {

    private val alarmServiceRingingState = AlarmServiceRingingState()

    @Test
    fun `given no alarm ringing, when ringing starts, then an alarm is ringing`() {
        // When
        alarmServiceRingingState.onRingingStarted()

        // Then
        assertThat(alarmServiceRingingState.isAnyAlarmRinging).isEqualTo(true)
    }

    @Test
    fun `given a ringing alarm, when ringing stops, then no alarm is ringing`() {
        // Given
        alarmServiceRingingState.onRingingStarted()

        // When
        alarmServiceRingingState.onRingingStopped()

        // Then
        assertThat(alarmServiceRingingState.isAnyAlarmRinging).isEqualTo(false)
    }
}
