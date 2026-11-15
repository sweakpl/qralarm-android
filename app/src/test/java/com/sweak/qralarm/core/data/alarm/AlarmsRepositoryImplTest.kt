package com.sweak.qralarm.core.data.alarm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.dao.CodesDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek

class AlarmsRepositoryImplTest {

    private val alarmsDao = mockk<AlarmsDao>(relaxUnitFun = true)
    private val codesDao = mockk<CodesDao>(relaxUnitFun = true)
    private val repository = AlarmsRepositoryImpl(alarmsDao, codesDao)

    @Test
    fun `given alarm with empty repeating days, when saved, then repeatingAlarmDays entity field is null`() = runTest {
        val slot = slot<AlarmEntity>()
        coEvery { alarmsDao.upsertAlarm(capture(slot)) } returns 1L

        val testAlarm = alarm(
            repeatingMode = Alarm.RepeatingMode.Days(emptyList())
        )
        repository.addOrEditAlarm(testAlarm)

        assertThat(slot.captured.repeatingAlarmDays).isNull()
    }

    @Test
    fun `given alarm with valid repeating days, when saved, then repeatingAlarmDays entity field contains day names`() = runTest {
        val slot = slot<AlarmEntity>()
        coEvery { alarmsDao.upsertAlarm(capture(slot)) } returns 1L

        val testAlarm = alarm(
            repeatingMode = Alarm.RepeatingMode.Days(listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        )
        repository.addOrEditAlarm(testAlarm)

        assertThat(slot.captured.repeatingAlarmDays).isEqualTo("MONDAY, FRIDAY")
    }

    @Test
    fun `given entity with empty string repeatingAlarmDays, when converted, then repeatingMode is Once without throwing exception`() = runTest {
        val entity = createTestAlarmEntity(repeatingAlarmDays = "")
        coEvery { alarmsDao.getAlarm(1L) } returns flowOf(entity)

        val result = repository.getAlarm(1L)

        assertThat(result?.repeatingMode).isEqualTo(Alarm.RepeatingMode.Once)
    }

    @Test
    fun `given entity with blank repeatingAlarmDays, when converted, then repeatingMode is Once without throwing exception`() = runTest {
        val entity = createTestAlarmEntity(repeatingAlarmDays = "   ")
        coEvery { alarmsDao.getAlarm(1L) } returns flowOf(entity)

        val result = repository.getAlarm(1L)

        assertThat(result?.repeatingMode).isEqualTo(Alarm.RepeatingMode.Once)
    }

    @Test
    fun `given entity with unknown day names, when converted, then it falls back to Once without throwing exception`() = runTest {
        val entity = createTestAlarmEntity(repeatingAlarmDays = "INVALID_DAY, NOT_A_DAY")
        coEvery { alarmsDao.getAlarm(1L) } returns flowOf(entity)

        val result = repository.getAlarm(1L)

        assertThat(result?.repeatingMode).isEqualTo(Alarm.RepeatingMode.Once)
    }

    @Test
    fun `given entity with mixed valid and invalid day names, when converted, then only valid days are retained`() = runTest {
        val entity = createTestAlarmEntity(repeatingAlarmDays = "MONDAY, INVALID_DAY, WEDNESDAY")
        coEvery { alarmsDao.getAlarm(1L) } returns flowOf(entity)

        val result = repository.getAlarm(1L)

        assertThat(result?.repeatingMode).isEqualTo(
            Alarm.RepeatingMode.Days(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
    }

    @Test
    fun `given entity with unknown ringtone name, when converted, then it falls back to default without throwing exception`() = runTest {
        val entity = createTestAlarmEntity(ringtone = "UNKNOWN_RINGTONE")
        coEvery { alarmsDao.getAlarm(1L) } returns flowOf(entity)

        val result = repository.getAlarm(1L)

        assertThat(result?.ringtone).isEqualTo(Alarm.Ringtone.GENTLE_GUITAR)
    }

    private fun createTestAlarmEntity(
        alarmId: Long = 1L,
        repeatingAlarmDays: String? = null,
        ringtone: String = Alarm.Ringtone.GENTLE_GUITAR.name
    ) = AlarmEntity(
        alarmId = alarmId,
        alarmHourOfDay = 7,
        alarmMinute = 30,
        isAlarmEnabled = true,
        isAlarmRunning = false,
        nextAlarmTimeInMillis = 1000L,
        repeatingAlarmDays = repeatingAlarmDays,
        numberOfSnoozes = 3,
        snoozeDurationInMinutes = 10,
        numberOfSnoozesLeft = 3,
        isAlarmSnoozed = false,
        nextSnoozedAlarmTimeInMillis = null,
        ringtone = ringtone,
        customRingtoneUriString = null,
        alarmVolumePercentage = 0,
        areVibrationsEnabled = true,
        isUsingCode = false,
        assignedCodeId = null,
        isOpenCodeLinkEnabled = false,
        cancelLockDurationInMinutes = 60,
        isEmergencyTaskEnabled = true,
        alarmLabel = null,
        gentleWakeUpDurationInSeconds = 0,
        temporaryMuteDurationInSeconds = 0,
        skipAlarmUntilTimeInMillis = null
    )
}
