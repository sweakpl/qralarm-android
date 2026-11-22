package com.sweak.qralarm.alarm.service.session

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AlarmVolumeControllerTest {

    enum class VolumeLevelCase(
        val minVolume: Int,
        val maxVolume: Int,
        val volumePercentage: Int,
        val expectedVolumeLevel: Int
    ) {
        FULL(minVolume = 1, maxVolume = 7, volumePercentage = 100, expectedVolumeLevel = 7),
        HALF(minVolume = 1, maxVolume = 7, volumePercentage = 50, expectedVolumeLevel = 4),
        FRACTION_TRUNCATED(minVolume = 0, maxVolume = 7, volumePercentage = 30, expectedVolumeLevel = 2),
        ZERO_PERCENT_STAYS_AUDIBLE(minVolume = 1, maxVolume = 7, volumePercentage = 0, expectedVolumeLevel = 2),
        TRUNCATED_TO_MIN_STAYS_AUDIBLE(minVolume = 0, maxVolume = 7, volumePercentage = 10, expectedVolumeLevel = 1)
    }

    @Test
    fun `given a stream range and a percentage, when computing the custom volume level, then it scales the range and stays above the minimum`(
        @TestParameter case: VolumeLevelCase
    ) {
        // When
        val volumeLevel = AlarmVolumeController.customVolumeLevel(
            minVolume = case.minVolume,
            maxVolume = case.maxVolume,
            volumePercentage = case.volumePercentage
        )

        // Then
        assertThat(volumeLevel).isEqualTo(case.expectedVolumeLevel)
    }
}
