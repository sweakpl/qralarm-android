package com.sweak.qralarm.alarm.service.session

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import javax.inject.Provider

class RingingSessionTest {

    private val alarmMuteController = mockk<AlarmMuteController>(relaxed = true)
    private val alarmVolumeController = mockk<AlarmVolumeController>(relaxed = true)
    private val players = mutableListOf<AlarmRingtonePlayer>()

    private val resumeRingingCallbacks = mutableListOf<() -> Unit>()

    init {
        val resumeRinging = slot<() -> Unit>()
        every {
            alarmMuteController.start(any(), capture(resumeRinging))
        } answers {
            resumeRingingCallbacks += resumeRinging.captured
        }
    }

    private val factory = RingingSession.Factory(
        alarmRingtonePlayerProvider = Provider {
            mockk<AlarmRingtonePlayer>(relaxed = true).also { players += it }
        },
        alarmMuteController = alarmMuteController,
        alarmVolumeController = alarmVolumeController
    )

    private fun AlarmRingtonePlayer.verifyPlayed(ringtone: Alarm.Ringtone, times: Int) {
        verify(exactly = times) {
            playAlarmRingtone(ringtone = ringtone, volumeIncreaseSeconds = any())
        }
    }

    @Test
    fun `given a muted ringing alarm, when the mute ends, then its own ringtone plays again`() {
        // Given
        val session = factory.create(
            alarm = alarm(ringtone = Alarm.Ringtone.KALIMBA).copy(alarmId = 7L)
        )
        session.start()

        // When
        resumeRingingCallbacks.single().invoke()

        // Then
        players.single().verifyPlayed(ringtone = Alarm.Ringtone.KALIMBA, times = 2)
    }

    @Test
    fun `given a muted alarm taken over by another alarm, when the first alarm's mute ends, then nothing plays on its behalf`() {
        // Given
        val firstSession = factory.create(
            alarm = alarm(ringtone = Alarm.Ringtone.KALIMBA).copy(alarmId = 7L)
        )
        firstSession.start()

        val secondSession = factory.create(
            alarm = alarm(ringtone = Alarm.Ringtone.ROOSTER).copy(alarmId = 8L)
        )
        firstSession.stop()
        secondSession.start()

        // When
        resumeRingingCallbacks.first().invoke()

        // Then
        val (firstPlayer, secondPlayer) = players
        firstPlayer.verifyPlayed(ringtone = Alarm.Ringtone.KALIMBA, times = 1)
        secondPlayer.verifyPlayed(ringtone = Alarm.Ringtone.ROOSTER, times = 1)
        secondPlayer.verifyPlayed(ringtone = Alarm.Ringtone.KALIMBA, times = 0)
    }

    @Test
    fun `given a stopped session, when stopped again, then nothing is torn down twice`() {
        // Given
        val session = factory.create(alarm = alarm().copy(alarmId = 7L))
        session.start()
        session.stop()

        // When
        session.stop()

        // Then
        verify(exactly = 1) {
            alarmMuteController.stop()
            alarmVolumeController.stop()
            players.single().onDestroy()
        }
    }
}
