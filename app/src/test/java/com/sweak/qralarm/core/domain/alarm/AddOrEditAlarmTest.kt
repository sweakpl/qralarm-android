package com.sweak.qralarm.core.domain.alarm

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(TestParameterInjector::class)
class AddOrEditAlarmTest {

    private val alarmsRepository = spyk(FakeAlarmsRepository())
    private val codesRepository = mockk<CodesRepository>(relaxUnitFun = true)
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val addOrEditAlarm = AddOrEditAlarm(
        alarmsRepository = alarmsRepository,
        codesRepository = codesRepository,
        alarmRingtoneStorage = alarmRingtoneStorage
    )

    @Test
    fun `given alarm with a new code, when saving, then the code is created and the stored alarm references its id`() = runTest {
        // Given
        val createdCodeId = 5L
        coEvery { codesRepository.findOrCreateCode("kitchen-qr") } returns createdCodeId
        coEvery { codesRepository.getCode(createdCodeId) } returns
                Code(codeId = createdCodeId, value = "kitchen-qr", name = null)
        val newCode = Code(codeId = 0, value = "kitchen-qr", name = null)

        // When
        val result = addOrEditAlarm(alarm(assignedCode = newCode))

        // Then
        assertThat(alarmsRepository.getAlarm(result.alarmId))
            .isNotNull()
            .prop(Alarm::assignedCode)
            .isEqualTo(newCode.copy(codeId = createdCodeId))
    }

    @Test
    fun `given alarm with an existing code whose name changed, when saving, then the code is renamed`() = runTest {
        // Given
        coEvery { codesRepository.getCode(7) } returns
                Code(codeId = 7, value = "kitchen-qr", name = "Kitchen")
        val renamedCode = Code(codeId = 7, value = "kitchen-qr", name = "Fridge")

        // When
        val result = addOrEditAlarm(alarm(assignedCode = renamedCode))

        // Then
        coVerify { codesRepository.updateCodeName(7, "Fridge") }
        assertThat(alarmsRepository.getAlarm(result.alarmId))
            .isNotNull()
            .prop(Alarm::assignedCode)
            .isEqualTo(renamedCode)
    }

    @Test
    fun `given alarm with an existing code whose name is unchanged, when saving, then the code is not renamed`() = runTest {
        // Given
        val code = Code(codeId = 7, value = "kitchen-qr", name = "Kitchen")
        coEvery { codesRepository.getCode(7) } returns code

        // When
        addOrEditAlarm(alarm(assignedCode = code))

        // Then
        coVerify(exactly = 0) { codesRepository.updateCodeName(any(), any()) }
    }

    @Test
    fun `given alarm with no code, when saving, then it is stored with no code`() = runTest {
        // Given
        val alarm = alarm(assignedCode = null)

        // When
        val result = addOrEditAlarm(alarm)

        // Then
        assertThat(alarmsRepository.getAlarm(result.alarmId))
            .isNotNull()
            .prop(Alarm::assignedCode)
            .isNull()
    }

    @Test
    fun `given alarm, when saving, then unreferenced codes are cleaned up after the alarm is stored`() = runTest {
        // Given
        val alarm = alarm()

        // When
        addOrEditAlarm(alarm)

        // Then
        coVerifyOrder {
            alarmsRepository.addOrEditAlarm(any())
            codesRepository.cleanupUnreferencedCodes()
        }
    }

    @Test
    fun `given no ringtone uri, when saving, then it succeeds without touching ringtone storage`() = runTest {
        // Given
        val alarm = alarm()

        // When
        val result = addOrEditAlarm(alarm, temporaryCustomRingtoneContentUriString = null)

        // Then
        assertThat(result).isInstanceOf<AddOrEditAlarm.Result.Success>()
        verify { alarmRingtoneStorage wasNot Called }
    }

    @Test
    fun `given ringtone uri, when saving, then the ringtone is saved under the alarm id and the alarm points at it`() = runTest {
        // Given
        every {
            alarmRingtoneStorage.saveContentUriForAlarm("picked-ringtone-uri", alarmId = any())
        } answers { "ringtone-uri-for-${secondArg<Long>()}" }

        // When
        val result = addOrEditAlarm(
            alarm(),
            temporaryCustomRingtoneContentUriString = "picked-ringtone-uri"
        )

        // Then
        assertThat(result).isInstanceOf<AddOrEditAlarm.Result.Success>()
        assertThat(alarmsRepository.getAlarm(result.alarmId))
            .isNotNull()
            .prop(Alarm::customRingtoneUriString)
            .isEqualTo("ringtone-uri-for-${result.alarmId}")
    }

    enum class HandledSaveFailure(val exception: Exception) {
        IO(IOException()),
        SECURITY(SecurityException()),
        NULL_POINTER(NullPointerException())
    }

    @Test
    fun `given ringtone storage fails with a handled exception, when saving, then the failure is reported and the alarm stays saved`(
        @TestParameter failure: HandledSaveFailure
    ) = runTest {
        // Given
        every { alarmRingtoneStorage.saveContentUriForAlarm(any(), any()) } throws failure.exception

        // When
        val result = addOrEditAlarm(
            alarm(customRingtoneUriString = null),
            temporaryCustomRingtoneContentUriString = "picked-ringtone-uri"
        )

        // Then
        assertThat(result).isInstanceOf<AddOrEditAlarm.Result.CustomRingtoneSaveFailed>()
        assertThat(alarmsRepository.getAlarm(result.alarmId))
            .isNotNull()
            .prop(Alarm::customRingtoneUriString)
            .isNull()
    }

    @Test
    fun `given ringtone storage fails with any other exception, when saving, then it is rethrown`() = runTest {
        // Given
        every {
            alarmRingtoneStorage.saveContentUriForAlarm(any(), any())
        } throws IllegalStateException()

        // When & Then
        assertFailure {
            addOrEditAlarm(alarm(), temporaryCustomRingtoneContentUriString = "picked-ringtone-uri")
        }.isInstanceOf<IllegalStateException>()
    }
}
