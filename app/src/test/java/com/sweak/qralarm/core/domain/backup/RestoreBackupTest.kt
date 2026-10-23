package com.sweak.qralarm.core.domain.backup

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.AlarmScheduler
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.fake.FakeBackupCodec
import com.sweak.qralarm.core.domain.fake.FakeBackupReader
import com.sweak.qralarm.core.domain.fixture.backupAlarm
import com.sweak.qralarm.core.domain.fixture.backupBundle
import com.sweak.qralarm.core.domain.fixture.backupMetadata
import com.sweak.qralarm.core.domain.fixture.backupPreferences
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.Theme
import com.sweak.qralarm.core.domain.util.Result
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(TestParameterInjector::class)
class RestoreBackupTest {

    private val backupRepository = mockk<BackupRepository>(relaxUnitFun = true)
    private val alarmsRepository = mockk<AlarmsRepository>(relaxUnitFun = true)
    private val codesRepository = mockk<CodesRepository>(relaxUnitFun = true)
    private val userDataRepository = mockk<UserDataRepository>(relaxUnitFun = true)
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val backupCodec = FakeBackupCodec()
    private val restoreBackup = RestoreBackup(
        backupRepository = backupRepository,
        alarmsRepository = alarmsRepository,
        codesRepository = codesRepository,
        userDataRepository = userDataRepository,
        alarmRingtoneStorage = alarmRingtoneStorage,
        alarmScheduler = alarmScheduler,
        backupCodec = backupCodec
    )

    private val backupUriString = "content://documents/backup"

    private fun givenRestore(
        bundle: BackupBundle = backupBundle(),
        metadata: BackupMetadata = backupMetadata(),
        backedUpRingtones: Map<Long, ByteArray> = emptyMap(),
        storedAlarmIds: List<Long> = emptyList(),
        alarmIdMap: Map<Long, Long> = emptyMap(),
        codeIdMap: Map<Long, Long> = emptyMap()
    ): FakeBackupReader {
        val backupReader = FakeBackupReader(
            metadata = metadata,
            bundle = bundle,
            ringtones = backedUpRingtones
        )
        backupCodec.addBackup(sourceUriString = backupUriString, backupReader = backupReader)
        coEvery { backupRepository.getAllAlarmIds() } returns storedAlarmIds
        coEvery { backupRepository.replaceAll(any(), any()) } returns
                RestoreIdMappings(alarmIdMap = alarmIdMap, codeIdMap = codeIdMap)

        return backupReader
    }

    enum class OpenFailure(val exception: Exception, val expectedError: BackupError) {
        NOT_A_QRALARM_BACKUP(
            BackupFormatException.NotAQRAlarmBackup(),
            BackupError.NotAQRAlarmBackup
        ),
        UNSUPPORTED_FORMAT_VERSION(
            BackupFormatException.UnsupportedFormatVersion(
                fileFormatVersion = CURRENT_BACKUP_FORMAT_VERSION + 1
            ),
            BackupError.UnsupportedFormatVersion(
                fileFormatVersion = CURRENT_BACKUP_FORMAT_VERSION + 1,
                supportedFormatVersion = CURRENT_BACKUP_FORMAT_VERSION
            )
        ),
        IO(IOException(), BackupError.IoFailure),
        SECURITY(SecurityException(), BackupError.IoFailure)
    }

    @Test
    fun `given the backup cannot be opened, when restoring, then the matching error is returned`(
        @TestParameter failure: OpenFailure
    ) = runTest {
        // Given
        backupCodec.openFailure = failure.exception

        // When
        val result = restoreBackup(sourceUriString = backupUriString)

        // Then
        assertThat(result).isEqualTo(Result.Error(failure.expectedError))
    }

    @Test
    fun `given the backup cannot be opened, when restoring, then nothing stored is touched`(
        @TestParameter failure: OpenFailure
    ) = runTest {
        // Given
        backupCodec.openFailure = failure.exception

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify {
            backupRepository wasNot Called
            alarmsRepository wasNot Called
            codesRepository wasNot Called
            userDataRepository wasNot Called
            alarmRingtoneStorage wasNot Called
            alarmScheduler wasNot Called
        }
    }

    @Test
    fun `given opening the backup fails with any other exception, when restoring, then it is rethrown`() = runTest {
        // Given
        backupCodec.openFailure = IllegalStateException()

        // When & Then
        assertFailure {
            restoreBackup(sourceUriString = backupUriString)
        }.isInstanceOf<IllegalStateException>()
    }

    @Test
    fun `given stored alarms, when restoring, then every one of them is cancelled`() = runTest {
        // Given
        givenRestore(storedAlarmIds = listOf(1, 2))

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify {
            alarmScheduler.cancelAlarm(alarmId = 1)
            alarmScheduler.cancelAlarm(alarmId = 2)
        }
    }

    @Test
    fun `given a backup, when restoring, then its codes and alarms replace the stored ones`() = runTest {
        // Given
        val bundle = backupBundle(
            alarms = listOf(backupAlarm(alarmId = 5), backupAlarm(alarmId = 6)),
            codes = listOf(BackupCode(codeId = 7, value = "kitchen-qr", name = "Kitchen"))
        )
        givenRestore(bundle = bundle)

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerify { backupRepository.replaceAll(codes = bundle.codes, alarms = bundle.alarms) }
    }

    @Test
    fun `given stored alarms, when restoring, then their ringtone files are deleted`() = runTest {
        // Given
        givenRestore(storedAlarmIds = listOf(1, 2))

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify {
            alarmRingtoneStorage.deleteForAlarm(alarmId = 1)
            alarmRingtoneStorage.deleteForAlarm(alarmId = 2)
        }
    }

    @Test
    fun `given replacing the stored alarms fails, when restoring, then their ringtone files are kept`() = runTest {
        // Given
        givenRestore(storedAlarmIds = listOf(1, 2))
        coEvery { backupRepository.replaceAll(any(), any()) } throws IllegalStateException()

        // When
        assertFailure { restoreBackup(sourceUriString = backupUriString) }

        // Then
        verify(exactly = 0) { alarmRingtoneStorage.deleteForAlarm(any()) }
    }

    @Test
    fun `given replacing the stored alarms fails, when restoring, then the stored alarms are not cancelled`() = runTest {
        // Given
        givenRestore(storedAlarmIds = listOf(1, 2))
        coEvery { backupRepository.replaceAll(any(), any()) } throws IOException()

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify { alarmScheduler wasNot Called }
    }

    @Test
    fun `given replacing the stored alarms fails, when restoring, then the default code is left as it was`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                codes = listOf(BackupCode(codeId = 7, value = "kitchen-qr", name = "Kitchen")),
                preferences = backupPreferences(defaultAlarmCodeId = 7)
            )
        )
        coEvery { backupRepository.replaceAll(any(), any()) } throws IOException()

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify { codesRepository wasNot Called }
    }

    @Test
    fun `given replacing the stored alarms fails, when restoring, then an IO failure is returned`() = runTest {
        // Given
        givenRestore()
        coEvery { backupRepository.replaceAll(any(), any()) } throws IOException()

        // When
        val result = restoreBackup(sourceUriString = backupUriString)

        // Then
        assertThat(result).isEqualTo(Result.Error(BackupError.IoFailure))
    }

    @Test
    fun `given a backed-up ringtone, when restoring, then it is written under the alarm's new id and assigned to it`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                alarms = listOf(backupAlarm(alarmId = 5, hasCustomRingtoneFile = true))
            ),
            backedUpRingtones = mapOf(5L to byteArrayOf(1, 2, 3)),
            alarmIdMap = mapOf(5L to 11L)
        )
        every { alarmRingtoneStorage.writeForAlarm(any(), any()) } answers
                { "ringtone-uri-for-${firstArg<Long>()}" }

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerify { alarmsRepository.setAlarmRingtoneUri(alarmId = 11, uri = "ringtone-uri-for-11") }
    }

    @Test
    fun `given a ringtone missing from the backup, when restoring, then the alarm falls back to the default ringtone`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                alarms = listOf(backupAlarm(alarmId = 5, hasCustomRingtoneFile = true))
            ),
            backedUpRingtones = emptyMap(),
            alarmIdMap = mapOf(5L to 11L)
        )

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerify {
            alarmsRepository.setAlarmRingtoneUri(alarmId = 11, uri = null)
            alarmsRepository.setAlarmRingtone(alarmId = 11, ringtone = Alarm.Ringtone.GENTLE_GUITAR)
        }
    }

    @Test
    fun `given a backed-up ringtone that cannot be written, when restoring, then the alarm falls back to the default ringtone`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                alarms = listOf(backupAlarm(alarmId = 5, hasCustomRingtoneFile = true))
            ),
            backedUpRingtones = mapOf(5L to byteArrayOf(1, 2, 3)),
            alarmIdMap = mapOf(5L to 11L)
        )
        every { alarmRingtoneStorage.writeForAlarm(11, any()) } returns null

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerify {
            alarmsRepository.setAlarmRingtoneUri(alarmId = 11, uri = null)
            alarmsRepository.setAlarmRingtone(alarmId = 11, ringtone = Alarm.Ringtone.GENTLE_GUITAR)
        }
    }

    @Test
    fun `given an alarm without a ringtone file, when restoring, then its ringtone is left alone`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                alarms = listOf(backupAlarm(alarmId = 5, hasCustomRingtoneFile = false))
            ),
            backedUpRingtones = mapOf(5L to byteArrayOf(1, 2, 3)),
            alarmIdMap = mapOf(5L to 11L)
        )

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify(exactly = 0) { alarmRingtoneStorage.writeForAlarm(any(), any()) }
        verify { alarmsRepository wasNot Called }
    }

    @Test
    fun `given settings in the backup, when restoring, then they are applied`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                preferences = backupPreferences(
                    emergencySliderRange = 20..80,
                    emergencyRequiredMatches = 4,
                    emergencyDisableRepeatingAlarms = true,
                    theme = Theme.Dynamic
                )
            )
        )

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerify {
            userDataRepository.setEmergencySliderRange(range = 20..80)
            userDataRepository.setEmergencyRequiredMatches(matches = 4)
            userDataRepository.setEmergencyDisableRepeatingAlarms(disable = true)
            userDataRepository.setTheme(theme = Theme.Dynamic)
        }
    }

    @Test
    fun `given settings missing from the backup, when restoring, then the current ones are left alone`() = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                preferences = backupPreferences(
                    emergencySliderRange = null,
                    emergencyRequiredMatches = null,
                    emergencyDisableRepeatingAlarms = null,
                    theme = null
                )
            )
        )

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        verify { userDataRepository wasNot Called }
    }

    enum class DefaultCodeCase(
        val backedUpDefaultCodeId: Long?,
        val expectedDefaultCodeId: Long?
    ) {
        REMAPPED_TO_NEW_ID(backedUpDefaultCodeId = 7, expectedDefaultCodeId = 21),
        NO_DEFAULT(backedUpDefaultCodeId = null, expectedDefaultCodeId = null),
        SKIPPED_DUPLICATE(backedUpDefaultCodeId = 8, expectedDefaultCodeId = null)
    }

    @Test
    fun `given the backup's default code, when restoring, then the default is set to its new id after the alarms are replaced`(
        @TestParameter case: DefaultCodeCase
    ) = runTest {
        // Given
        givenRestore(
            bundle = backupBundle(
                codes = listOf(
                    BackupCode(codeId = 7, value = "kitchen-qr", name = "Kitchen"),
                    BackupCode(codeId = 8, value = "kitchen-qr", name = "Kitchen again")
                ),
                preferences = backupPreferences(defaultAlarmCodeId = case.backedUpDefaultCodeId)
            ),
            codeIdMap = mapOf(7L to 21L)
        )

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        coVerifyOrder {
            backupRepository.replaceAll(any(), any())
            codesRepository.setDefaultAlarmCodeById(codeId = case.expectedDefaultCodeId)
        }
    }

    @Test
    fun `given a backup, when restoring, then it succeeds`() = runTest {
        // Given
        givenRestore()

        // When
        val result = restoreBackup(sourceUriString = backupUriString)

        // Then
        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `given a backup, when restoring, then the backup reader is closed`() = runTest {
        // Given
        val backupReader = givenRestore()

        // When
        restoreBackup(sourceUriString = backupUriString)

        // Then
        assertThat(backupReader.isClosed).isTrue()
    }
}
