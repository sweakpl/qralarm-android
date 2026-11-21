package com.sweak.qralarm.alarm.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sweak.qralarm.alarm.service.AlarmMuteController.MuteRequest
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmMuteControllerTest {

    private val dispatcher = StandardTestDispatcher()

    private val controller = AlarmMuteController(context = mockk())

    private var stopRingingCount = 0
    private var resumeRingingCount = 0

    private fun startSession(resumeRinging: suspend () -> Unit = { resumeRingingCount++ }) {
        controller.startSession(
            stopRinging = { stopRingingCount++ },
            resumeRinging = resumeRinging,
            dispatcher = dispatcher
        )
    }

    private fun TestScope.advanceTimeToMillis(millis: Long) {
        advanceTimeBy((millis - testScheduler.currentTime).milliseconds)
        runCurrent()
    }

    @Test
    fun `given a ringing alarm, when muted temporarily, then ringing stops at once and resumes after the requested duration`() =
        runTest(dispatcher) {
            // Given
            startSession()

            // When
            controller.mute(MuteRequest.Temporary(durationSeconds = 20))

            // Then
            assertThat(stopRingingCount).isEqualTo(1)

            advanceTimeToMillis(19_999L)
            assertThat(resumeRingingCount).isEqualTo(0)

            advanceTimeToMillis(20_000L)
            assertThat(resumeRingingCount).isEqualTo(1)
        }

    @Test
    fun `given an alarm already muted temporarily once, when muted temporarily again, then the request is ignored`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))
            advanceTimeToMillis(15_000L)

            // When
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))

            // Then
            assertThat(stopRingingCount).isEqualTo(1)

            advanceTimeToMillis(60_000L)
            assertThat(resumeRingingCount).isEqualTo(1)
        }

    @Test
    fun `given a temporary mute in progress, when muted for the emergency task, then ringing resumes 10 seconds after the emergency mute only`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.Temporary(durationSeconds = 30))
            advanceTimeToMillis(5_000L)

            // When
            controller.mute(MuteRequest.EmergencyTask)

            // Then
            assertThat(stopRingingCount).isEqualTo(2)

            advanceTimeToMillis(14_999L)
            assertThat(resumeRingingCount).isEqualTo(0)

            advanceTimeToMillis(15_000L)
            assertThat(resumeRingingCount).isEqualTo(1)

            advanceTimeToMillis(60_000L)
            assertThat(resumeRingingCount).isEqualTo(1)
        }

    @Test
    fun `given an emergency task mute in progress, when muted temporarily for the first time, then the temporary mute replaces it`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.EmergencyTask)
            advanceTimeToMillis(5_000L)

            // When
            controller.mute(MuteRequest.Temporary(durationSeconds = 30))

            // Then
            assertThat(stopRingingCount).isEqualTo(2)

            advanceTimeToMillis(34_999L)
            assertThat(resumeRingingCount).isEqualTo(0)

            advanceTimeToMillis(35_000L)
            assertThat(resumeRingingCount).isEqualTo(1)
        }

    @Test
    fun `given an emergency task mute in progress, when muted for the emergency task again, then the 10 seconds restart`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.EmergencyTask)
            advanceTimeToMillis(6_000L)

            // When
            controller.mute(MuteRequest.EmergencyTask)

            // Then
            advanceTimeToMillis(15_999L)
            assertThat(resumeRingingCount).isEqualTo(0)

            advanceTimeToMillis(16_000L)
            assertThat(resumeRingingCount).isEqualTo(1)
        }

    @Test
    fun `given a mute in progress, when the session stops, then ringing never resumes`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))
            advanceTimeToMillis(5_000L)

            // When
            controller.stopSession()

            // Then
            advanceTimeToMillis(60_000L)
            assertThat(resumeRingingCount).isEqualTo(0)
        }

    @Test
    fun `given a mute whose resume is still in progress, when the session stops, then the resume is cancelled`() =
        runTest(dispatcher) {
            // Given
            val resumeGate = CompletableDeferred<Unit>()
            startSession(
                resumeRinging = {
                    resumeGate.await()
                    resumeRingingCount++
                }
            )
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))
            advanceTimeToMillis(15_000L)

            // When
            controller.stopSession()
            resumeGate.complete(Unit)

            // Then
            advanceTimeToMillis(60_000L)
            assertThat(resumeRingingCount).isEqualTo(0)
        }

    @Test
    fun `given a previous session that used its temporary mute, when a new session starts, then a temporary mute is allowed again`() =
        runTest(dispatcher) {
            // Given
            startSession()
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))
            controller.stopSession()

            // When
            startSession()
            controller.mute(MuteRequest.Temporary(durationSeconds = 15))

            // Then
            assertThat(stopRingingCount).isEqualTo(2)
        }
}
