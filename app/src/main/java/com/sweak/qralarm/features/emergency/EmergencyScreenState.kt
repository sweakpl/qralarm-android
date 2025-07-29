package com.sweak.qralarm.features.emergency

import com.sweak.qralarm.features.emergency.util.EMERGENCY_TASK_INITIAL_REMAINING_MATCHES
import com.sweak.qralarm.features.emergency.util.EMERGENCY_TASK_VALUE_RANGE

data class EmergencyScreenState(
    val isTaskStarted: Boolean = false,
    val emergencyTaskConfig: EmergencyTaskConfig = EmergencyTaskConfig(),
) {
    data class EmergencyTaskConfig(
        val valueRange: IntRange = EMERGENCY_TASK_VALUE_RANGE,
        val targetValue: Int = 100,
        val currentValue: Int = 50,
        val remainingMatches: Int = EMERGENCY_TASK_INITIAL_REMAINING_MATCHES,
        val isCompleted: Boolean = false
    )
}
