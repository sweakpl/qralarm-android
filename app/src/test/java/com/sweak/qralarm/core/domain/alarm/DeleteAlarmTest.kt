package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isNull
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteAlarmTest {

    private val alarmsRepository = spyk(FakeAlarmsRepository())
    private val codesRepository = mockk<CodesRepository>(relaxUnitFun = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val deleteAlarm = DeleteAlarm(
        alarmsRepository = alarmsRepository,
        codesRepository = codesRepository,
        alarmScheduler = alarmScheduler,
        alarmRingtoneStorage = alarmRingtoneStorage
    )

    @Test
    fun `given stored alarm, when deleting, then the scheduled alarm is cancelled`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm())

        // When
        deleteAlarm(alarmId)

        // Then
        verify { alarmScheduler.cancelAlarm(alarmId) }
    }

    @Test
    fun `given stored alarm, when deleting, then the alarm is removed`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(alarm())

        // When
        deleteAlarm(alarmId)

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId)).isNull()
    }

    @Test
    fun `given stored alarm, when deleting, then its ringtone file is deleted`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "file://ringtones/alarm")
        )

        // When
        deleteAlarm(alarmId)

        // Then
        verify { alarmRingtoneStorage.deleteForAlarm(alarmId) }
    }

    @Test
    fun `given stored alarm, when deleting, then unreferenced codes are cleaned up after the alarm is removed`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(assignedCode = Code(codeId = 7, value = "kitchen-qr", name = "Kitchen"))
        )

        // When
        deleteAlarm(alarmId)

        // Then
        coVerifyOrder {
            alarmsRepository.deleteAlarm(alarmId)
            codesRepository.cleanupUnreferencedCodes()
        }
    }
}
