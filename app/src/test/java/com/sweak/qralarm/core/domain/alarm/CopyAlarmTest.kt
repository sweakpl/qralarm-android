package com.sweak.qralarm.core.domain.alarm

import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.prop
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CopyAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()
    private val alarmRingtoneStorage = mockk<AlarmRingtoneStorage>(relaxUnitFun = true)
    private val copyAlarm = CopyAlarm(
        alarmsRepository = alarmsRepository,
        alarmRingtoneStorage = alarmRingtoneStorage
    )

    @Test
    fun `given no alarm with the id, when copying, then nothing is stored and ringtone storage is not touched`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        copyAlarm(missingAlarmId)

        // Then
        assertThat(alarmsRepository.getAllAlarms().first()).isEmpty()
        verify { alarmRingtoneStorage wasNot Called }
    }

    @Test
    fun `given enabled running alarm with a skipped occurrence, when copying, then the copy is a new disabled idle alarm with every other setting kept`() = runTest {
        // Given
        val sourceAlarm = alarm(
            isAlarmEnabled = true,
            isAlarmRunning = true,
            assignedCode = Code(codeId = 7, value = "kitchen-qr", name = "Kitchen"),
            skipAlarmUntilTimeInMillis = 1_000
        )
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(sourceAlarm)

        // When
        copyAlarm(sourceAlarmId)

        // Then
        val copiedAlarm = copiedAlarm(sourceAlarmId)
        assertThat(copiedAlarm).isEqualTo(
            sourceAlarm.copy(
                alarmId = copiedAlarm.alarmId,
                isAlarmEnabled = false,
                isAlarmRunning = false,
                skipAlarmUntilTimeInMillis = null
            )
        )
    }

    @Test
    fun `given snoozed alarm, when copying, then the copy is not snoozed and has its snoozes replenished`() = runTest {
        // Given
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                numberOfSnoozes = 3,
                numberOfSnoozesLeft = 1,
                isAlarmSnoozed = true,
                nextSnoozedAlarmTimeInMillis = 1_000
            )
        )

        // When
        copyAlarm(sourceAlarmId)

        // Then
        assertThat(copiedAlarm(sourceAlarmId).snoozeConfig).all {
            prop(Alarm.SnoozeConfig::numberOfSnoozesLeft).isEqualTo(3)
            prop(Alarm.SnoozeConfig::isAlarmSnoozed).isFalse()
            prop(Alarm.SnoozeConfig::nextSnoozedAlarmTimeInMillis).isNull()
        }
    }

    @Test
    fun `given alarm with a custom ringtone, when copying, then the source alarm is left unchanged`() = runTest {
        // Given
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "source-ringtone-uri")
        )
        val storedSourceAlarm = alarmsRepository.getAlarm(sourceAlarmId)
        every { alarmRingtoneStorage.duplicateForAlarm(any(), any()) } returns "copied-ringtone-uri"

        // When
        copyAlarm(sourceAlarmId)

        // Then
        assertThat(alarmsRepository.getAlarm(sourceAlarmId)).isEqualTo(storedSourceAlarm)
    }

    @Test
    fun `given alarm without a custom ringtone, when copying, then ringtone storage is not touched`() = runTest {
        // Given
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(alarm(customRingtoneUriString = null))

        // When
        copyAlarm(sourceAlarmId)

        // Then
        verify { alarmRingtoneStorage wasNot Called }
    }

    @Test
    fun `given alarm with a custom ringtone, when copying, then the ringtone is duplicated and the copy points at the new file`() = runTest {
        // Given
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(
            alarm(customRingtoneUriString = "source-ringtone-uri")
        )
        every {
            alarmRingtoneStorage.duplicateForAlarm(sourceAlarmId = sourceAlarmId, newAlarmId = any())
        } answers { "ringtone-uri-for-${secondArg<Long>()}" }

        // When
        copyAlarm(sourceAlarmId)

        // Then
        val copiedAlarm = copiedAlarm(sourceAlarmId)
        assertThat(copiedAlarm.customRingtoneUriString)
            .isEqualTo("ringtone-uri-for-${copiedAlarm.alarmId}")
    }

    @Test
    fun `given custom ringtone duplication fails, when copying, then the copy falls back to the default ringtone`() = runTest {
        // Given
        val sourceAlarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                ringtone = Alarm.Ringtone.CUSTOM_SOUND,
                customRingtoneUriString = "source-ringtone-uri"
            )
        )
        every { alarmRingtoneStorage.duplicateForAlarm(any(), any()) } returns null

        // When
        copyAlarm(sourceAlarmId)

        // Then
        assertThat(copiedAlarm(sourceAlarmId)).all {
            prop(Alarm::ringtone).isEqualTo(Alarm.Ringtone.GENTLE_GUITAR)
            prop(Alarm::customRingtoneUriString).isNull()
        }
    }

    private suspend fun copiedAlarm(sourceAlarmId: Long): Alarm =
        alarmsRepository.getAllAlarms().first().single { it.alarmId != sourceAlarmId }
}
