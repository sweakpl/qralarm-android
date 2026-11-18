package com.sweak.qralarm.features.emergency.task

import com.sweak.qralarm.core.domain.emergency.ExplanationValidator
import com.sweak.qralarm.core.domain.user.EMERGENCY_DEFAULT_REQUIRED_MATCHES
import com.sweak.qralarm.core.domain.user.EMERGENCY_DEFAULT_REQUIRED_MATCHES_RANGE
import com.sweak.qralarm.core.domain.user.model.EmergencyTaskType

data class EmergencyScreenState(
    val isTaskStarted: Boolean = false,
    /** Null until the setting has loaded. */
    val taskType: EmergencyTaskType? = null,
    val emergencyTaskConfig: EmergencyTaskConfig = EmergencyTaskConfig(),
    val explanation: ExplanationState = ExplanationState(),
    /** Shown when the alarm is about to ring again because nothing was typed or moved. */
    val idleSecondsRemaining: Int? = null,
    val isAlarmHeldSilent: Boolean = false,
    val showRingAgainChoice: Boolean = false
) {
    data class EmergencyTaskConfig(
        val valueRange: IntRange = EMERGENCY_DEFAULT_REQUIRED_MATCHES_RANGE,
        val targetValue: Int = 100,
        val currentValue: Int = 50,
        val remainingMatches: Int = EMERGENCY_DEFAULT_REQUIRED_MATCHES,
        val isCompleted: Boolean = false
    )

    data class ExplanationState(
        val text: String = "",
        val wordCount: Int = 0,
        /** Shown after a submit attempt, so the user is not nagged while still typing. */
        val issues: List<ExplanationValidator.Issue> = emptyList(),
        val pasteRejected: Boolean = false,
        val typingSecondsRemaining: Int = MIN_TYPING_SECONDS,
        val isCompleted: Boolean = false
    ) {
        val canSubmit: Boolean
            get() = !isCompleted && typingSecondsRemaining <= 0 &&
                wordCount in ExplanationValidator.MIN_WORDS..ExplanationValidator.MAX_WORDS
    }

    companion object {
        const val MIN_TYPING_SECONDS = 90
    }
}
