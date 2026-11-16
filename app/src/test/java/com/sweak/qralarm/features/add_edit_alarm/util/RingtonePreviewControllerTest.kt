package com.sweak.qralarm.features.add_edit_alarm.util

import android.net.Uri
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

class RingtonePreviewControllerTest {

    private val onPreviewCompleted = slot<(Boolean) -> Unit>()
    private val alarmRingtonePlayer = mockk<AlarmRingtonePlayer>(relaxed = true) {
        every {
            playAlarmRingtonePreview(
                ringtone = any(),
                onPreviewCompleted = capture(onPreviewCompleted)
            )
        } returns Unit
        every {
            playAlarmRingtonePreview(
                alarmRingtoneUri = any(),
                onPreviewCompleted = capture(onPreviewCompleted)
            )
        } returns Unit
    }
    private val controller = RingtonePreviewController(alarmRingtonePlayer)

    @Test
    fun `given the system default ringtone is unavailable, when the available ringtones are read, then it is left out`() {
        // Given
        every { alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable() } returns false

        // When
        val availableRingtones = controller.getAvailableRingtones()

        // Then
        assertThat(availableRingtones).isEqualTo(Ringtone.entries - Ringtone.SYSTEM_DEFAULT)
    }

    @Test
    fun `given a ringtone is playing, when it is toggled again, then playback stops`() {
        // Given
        controller.toggle(ringtone = Ringtone.KALIMBA, customRingtoneUri = null, onPlaybackError = {})

        // When
        controller.toggle(ringtone = Ringtone.KALIMBA, customRingtoneUri = null, onPlaybackError = {})

        // Then
        verify(exactly = 1) { alarmRingtonePlayer.stop() }
        assertThat(controller.playingRingtone.value).isNull()
    }

    @Test
    fun `given a ringtone is playing, when another one is toggled, then the other one plays`() {
        // Given
        controller.toggle(ringtone = Ringtone.KALIMBA, customRingtoneUri = null, onPlaybackError = {})

        // When
        controller.toggle(ringtone = Ringtone.ROOSTER, customRingtoneUri = null, onPlaybackError = {})

        // Then
        verify(exactly = 0) { alarmRingtonePlayer.stop() }
        verify { alarmRingtonePlayer.playAlarmRingtonePreview(Ringtone.ROOSTER, any()) }
        assertThat(controller.playingRingtone.value).isEqualTo(Ringtone.ROOSTER)
    }

    @Test
    fun `given a custom ringtone uri, when the custom sound is toggled, then the uri is played`() {
        // Given
        val customRingtoneUri = mockk<Uri>()

        // When
        controller.toggle(
            ringtone = Ringtone.CUSTOM_SOUND,
            customRingtoneUri = customRingtoneUri,
            onPlaybackError = {}
        )

        // Then
        verify { alarmRingtonePlayer.playAlarmRingtonePreview(customRingtoneUri, any()) }
        assertThat(controller.playingRingtone.value).isEqualTo(Ringtone.CUSTOM_SOUND)
    }

    @Test
    fun `given a ringtone is playing, when its preview fails, then the error is reported and nothing is playing`() {
        // Given
        val reportedErrors = mutableListOf<Unit>()
        controller.toggle(
            ringtone = Ringtone.KALIMBA,
            customRingtoneUri = null,
            onPlaybackError = { reportedErrors += Unit }
        )

        // When
        onPreviewCompleted.captured(true)

        // Then
        assertThat(reportedErrors).containsExactly(Unit)
        assertThat(controller.playingRingtone.value).isNull()
    }

    @Test
    fun `given a ringtone is playing, when its preview ends, then no error is reported and nothing is playing`() {
        // Given
        val reportedErrors = mutableListOf<Unit>()
        controller.toggle(
            ringtone = Ringtone.KALIMBA,
            customRingtoneUri = null,
            onPlaybackError = { reportedErrors += Unit }
        )

        // When
        onPreviewCompleted.captured(false)

        // Then
        assertThat(reportedErrors).isEqualTo(emptyList())
        assertThat(controller.playingRingtone.value).isNull()
    }
}
