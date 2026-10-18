package com.sweak.qralarm.core.domain.alarm

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.epochMillisAt
import com.sweak.qralarm.core.domain.fixture.fixedClock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(TestParameterInjector::class)
class CanManipulateAlarmTest {

    private val alarmsRepository = FakeAlarmsRepository()

    private suspend fun canManipulateAlarm(alarmId: Long, now: LocalDateTime) =
        CanManipulateAlarm(alarmsRepository = alarmsRepository, clock = fixedClock(now))(
            alarmId = alarmId
        )

    enum class UnlockedCase(
        val isUsingCode: Boolean,
        val isAlarmEnabled: Boolean,
        val cancelLockDurationInMinutes: Int
    ) {
        NOT_USING_CODE(isUsingCode = false, isAlarmEnabled = true, cancelLockDurationInMinutes = 30),
        DISABLED(isUsingCode = true, isAlarmEnabled = false, cancelLockDurationInMinutes = 30),
        NO_CANCEL_LOCK(isUsingCode = true, isAlarmEnabled = true, cancelLockDurationInMinutes = 0)
    }

    @Test
    fun `given no alarm with the id, when checking, then it can't be manipulated`() = runTest {
        // Given
        val missingAlarmId = 42L

        // When
        val result = canManipulateAlarm(missingAlarmId, now = TODAY.atTime(7, 0))

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `given alarm without an active cancel lock, when checking, then it can be manipulated even when it is already due`(
        @TestParameter case: UnlockedCase
    ) = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = case.isUsingCode,
                isAlarmEnabled = case.isAlarmEnabled,
                cancelLockDurationInMinutes = case.cancelLockDurationInMinutes,
                nextAlarmTimeInMillis = epochMillisAt(TODAY.atTime(6, 59))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = TODAY.atTime(7, 0))

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `given locked alarm further away than the cancel lock, when checking, then it can be manipulated`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = true,
                isAlarmEnabled = true,
                cancelLockDurationInMinutes = 30,
                nextAlarmTimeInMillis = epochMillisAt(TODAY.atTime(8, 15))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = TODAY.atTime(7, 0))

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `given locked alarm exactly the cancel lock duration away, when checking, then it can be manipulated`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = true,
                isAlarmEnabled = true,
                cancelLockDurationInMinutes = 30,
                nextAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 30))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = TODAY.atTime(7, 0))

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `given locked alarm inside the cancel lock window, when checking, then it can't be manipulated`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = true,
                isAlarmEnabled = true,
                cancelLockDurationInMinutes = 30,
                nextAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 10))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = TODAY.atTime(7, 0))

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `given locked alarm a second short of the cancel lock duration away, when checking, then it can't be manipulated`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = true,
                isAlarmEnabled = true,
                cancelLockDurationInMinutes = 30,
                nextAlarmTimeInMillis = epochMillisAt(TODAY.atTime(7, 30))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = TODAY.atTime(7, 0, 1))

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `given locked alarm after the spring-forward night, when checking, then the time until it is measured in real time`() = runTest {
        // Given
        val alarmId = alarmsRepository.addOrEditAlarm(
            alarm(
                isUsingCode = true,
                isAlarmEnabled = true,
                cancelLockDurationInMinutes = 30,
                nextAlarmTimeInMillis = epochMillisAt(SPRING_FORWARD_DAY.atTime(3, 10))
            )
        )

        // When
        val result = canManipulateAlarm(alarmId, now = SPRING_FORWARD_DAY.atTime(1, 50))

        // Then
        assertThat(result).isFalse()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 10, 17)
        val SPRING_FORWARD_DAY: LocalDate = LocalDate.of(2026, 3, 29)
    }
}
