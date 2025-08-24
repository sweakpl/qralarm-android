package com.sweak.qralarm.features.emergency.task

import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_DEFAULT_REQUIRED_MATCHES
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_DEFAULT_SLIDER_RANGE

data class EmergencyScreenState(
    val isTaskStarted: Boolean = false,
    val emergencyTaskConfig: EmergencyTaskConfig = EmergencyTaskConfig(),
) {
    data class EmergencyTaskConfig(
        val valueRange: IntRange = EMERGENCY_DEFAULT_SLIDER_RANGE,
        val targetValue: Int = 100,
        val currentValue: Int = 50,
        val remainingMatches: Int = EMERGENCY_DEFAULT_REQUIRED_MATCHES,
        val isCompleted: Boolean = false
    )
}
