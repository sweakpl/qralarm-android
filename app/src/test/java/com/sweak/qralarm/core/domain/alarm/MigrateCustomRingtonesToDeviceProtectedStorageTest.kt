package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MigrateCustomRingtonesToDeviceProtectedStorageTest {

    private val alarmsRepository = spyk(FakeAlarmsRepository())
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val migrateCustomRingtonesToDeviceProtectedStorage =
        MigrateCustomRingtonesToDeviceProtectedStorage(
            alarmsRepository = alarmsRepository,
            alarmRingtoneStorage = alarmRingtoneStorage
        )

    @Test
    fun `given alarm without a custom ringtone, when migrating, then ringtone storage is not asked`() = runTest {
        // Given
        alarmsRepository.addOrEditAlarm(alarm(customRingtoneUriString = null))

        // When
        migrateCustomRingtonesToDeviceProtectedStorage()

        // Then
        verify { alarmRingtoneStorage wasNot Called }
    }

    @Test
    fun `given alarm without a custom ringtone stored before one with, when migrating, then the later alarm is still migrated`() = runTest {
        // Given
        alarmsRepository.addOrEditAlarm(alarm(customRingtoneUriString = null))
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "legacy-ringtone-uri")
        )
        every { alarmRingtoneStorage.migrateToDeviceProtectedStorage(alarmId) } returns
                "device-protected-ringtone-uri"

        // When
        migrateCustomRingtonesToDeviceProtectedStorage()

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::customRingtoneUriString)
            .isEqualTo("device-protected-ringtone-uri")
    }

    @Test
    fun `given ringtone moved to device protected storage, when migrating, then the alarm points at the new uri`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "legacy-ringtone-uri")
        )
        every { alarmRingtoneStorage.migrateToDeviceProtectedStorage(alarmId) } returns
                "device-protected-ringtone-uri"

        // When
        migrateCustomRingtonesToDeviceProtectedStorage()

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::customRingtoneUriString)
            .isEqualTo("device-protected-ringtone-uri")
    }

    @Test
    fun `given ringtone already in device protected storage, when migrating, then the alarm is not written`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "device-protected-ringtone-uri")
        )
        every { alarmRingtoneStorage.migrateToDeviceProtectedStorage(alarmId) } returns
                "device-protected-ringtone-uri"

        // When
        migrateCustomRingtonesToDeviceProtectedStorage()

        // Then
        coVerify(exactly = 0) { alarmsRepository.setAlarmRingtoneUri(any(), any()) }
    }

    @Test
    fun `given ringtone migration fails, when migrating, then the alarm keeps its stored uri`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "legacy-ringtone-uri")
        )
        every { alarmRingtoneStorage.migrateToDeviceProtectedStorage(alarmId) } returns null

        // When
        migrateCustomRingtonesToDeviceProtectedStorage()

        // Then
        assertThat(alarmsRepository.getAlarm(alarmId))
            .isNotNull()
            .prop(Alarm::customRingtoneUriString)
            .isEqualTo("legacy-ringtone-uri")
    }
}
