package com.sweak.qralarm.core.domain.backup

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.Code
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.fake.FakeBackupCodec
import com.sweak.qralarm.core.domain.fixture.backupAlarm
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.Theme
import com.sweak.qralarm.core.domain.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(TestParameterInjector::class)
class CreateBackupTest {

    private val backupRepository = mockk<BackupRepository>(relaxUnitFun = true)
    private val codesRepository = mockk<CodesRepository>(relaxUnitFun = true)
    private val userDataRepository = mockk<UserDataRepository>(relaxUnitFun = true)
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val backupCodec = FakeBackupCodec()
    private val createBackup = CreateBackup(
        backupRepository = backupRepository,
        codesRepository = codesRepository,
        userDataRepository = userDataRepository,
        alarmRingtoneStorage = alarmRingtoneStorage,
        backupCodec = backupCodec
    )

    private fun givenStoredData(
        alarms: List<BackupAlarm> = emptyList(),
        codes: List<BackupCode> = emptyList(),
        defaultAlarmCode: Code? = null,
        emergencySliderRange: IntRange = 0..100,
        emergencyRequiredMatches: Int = 1,
        isEmergencyDisableRepeatingAlarmsEnabled: Boolean = false,
        theme: Theme = Theme.Default
    ) {
        coEvery { backupRepository.readAlarmsForBackup() } returns alarms
        coEvery { backupRepository.readCodesForBackup() } returns codes
        every { codesRepository.getDefaultAlarmCodeFlow() } returns flowOf(defaultAlarmCode)
        every { userDataRepository.emergencySliderRange } returns flowOf(emergencySliderRange)
        every { userDataRepository.emergencyRequiredMatches } returns
                flowOf(emergencyRequiredMatches)
        every { userDataRepository.isEmergencyDisableRepeatingAlarmsEnabled } returns
                flowOf(isEmergencyDisableRepeatingAlarmsEnabled)
        every { userDataRepository.theme } returns flowOf(theme)
    }

    @Test
    fun `given stored alarms, codes and settings, when creating backup, then the bundle written to the destination holds them`() = runTest {
        // Given
        val alarms = listOf(backupAlarm(alarmId = 1), backupAlarm(alarmId = 2))
        val codes = listOf(
            BackupCode(codeId = 7, value = "kitchen-qr", name = "Kitchen"),
            BackupCode(codeId = 8, value = "bathroom-qr", name = null)
        )
        givenStoredData(
            alarms = alarms,
            codes = codes,
            defaultAlarmCode = Code(codeId = 7, value = "kitchen-qr", name = "Kitchen"),
            emergencySliderRange = 20..80,
            emergencyRequiredMatches = 4,
            isEmergencyDisableRepeatingAlarmsEnabled = true,
            theme = Theme.Dynamic
        )

        // When
        createBackup(destinationUriString = "content://documents/backup")

        // Then
        assertThat(backupCodec.writtenBundles["content://documents/backup"]).isEqualTo(
            BackupBundle(
                alarms = alarms,
                codes = codes,
                preferences = BackupPreferences(
                    defaultAlarmCodeId = 7,
                    emergencySliderRange = 20..80,
                    emergencyRequiredMatches = 4,
                    emergencyDisableRepeatingAlarms = true,
                    theme = Theme.Dynamic
                )
            )
        )
    }

    @Test
    fun `given no default code, when creating backup, then the written bundle has no default code id`() = runTest {
        // Given
        givenStoredData(defaultAlarmCode = null)

        // When
        createBackup(destinationUriString = "content://documents/backup")

        // Then
        assertThat(backupCodec.writtenBundles["content://documents/backup"])
            .isNotNull()
            .prop(BackupBundle::preferences)
            .prop(BackupPreferences::defaultAlarmCodeId)
            .isNull()
    }

    @Test
    fun `given writing succeeds, when creating backup, then it succeeds`() = runTest {
        // Given
        givenStoredData()
        backupCodec.writeFailure = null

        // When
        val result = createBackup(destinationUriString = "content://documents/backup")

        // Then
        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    enum class HandledWriteFailure(val exception: Exception) {
        IO(IOException()),
        SECURITY(SecurityException())
    }

    @Test
    fun `given writing fails with a handled exception, when creating backup, then an IO failure is returned`(
        @TestParameter failure: HandledWriteFailure
    ) = runTest {
        // Given
        givenStoredData()
        backupCodec.writeFailure = failure.exception

        // When
        val result = createBackup(destinationUriString = "content://documents/backup")

        // Then
        assertThat(result).isEqualTo(Result.Error(BackupError.IoFailure))
    }

    @Test
    fun `given writing fails with any other exception, when creating backup, then it is rethrown`() = runTest {
        // Given
        givenStoredData()
        backupCodec.writeFailure = IllegalStateException()

        // When & Then
        assertFailure {
            createBackup(destinationUriString = "content://documents/backup")
        }.isInstanceOf<IllegalStateException>()
    }
}
