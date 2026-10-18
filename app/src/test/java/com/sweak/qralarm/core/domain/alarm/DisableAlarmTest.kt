package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DisableAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val disableAlarm = DisableAlarm(
        alarmScheduler = alarmScheduler,
        alarmsRepository = alarmsRepository
    )

    @Test
    fun `given enabled alarm, when disabling, then the scheduled alarm is cancelled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm())

        // When
        disableAlarm(alarmId)

        // Then
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given enabled alarm, when disabling, then it is stored as disabled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm(isAlarmEnabled = true))

        // When
        disableAlarm(alarmId)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::isAlarmEnabled)
            .isFalse()
    }

    @Test
    fun `given snoozed enabled alarm, when disabling, then it is stored as not snoozed`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = true,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = 1_000
            )
        )

        // When
        disableAlarm(alarmId)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
    }

    @Test
    fun `given snoozed one-time alarm already disabled when it rang, when disabling, then it is stored as not snoozed`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isAlarmEnabled = false,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = 1_000
            )
        )

        // When
        disableAlarm(alarmId)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::snoozeConfig)
            .prop(Alarm.SnoozeConfig::isAlarmSnoozed)
            .isFalse()
    }

    @Test
    fun `given no alarm with the id, when disabling, then the scheduled alarm is still cancelled`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        disableAlarm(missingAlarmId)

        // Then
        verify { alarmScheduler.cancelAlarm(missingAlarmId) }
    }
}
