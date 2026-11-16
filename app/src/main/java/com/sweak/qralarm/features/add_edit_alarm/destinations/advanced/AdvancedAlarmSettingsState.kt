package com.sweak.qralarm.features.add_edit_alarm.destinations.advanced

import com.sweak.qralarm.core.domain.alarm.AVAILABLE_CANCEL_LOCK_DURATIONS_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft

data class AdvancedAlarmSettingsState(
    val draft: AlarmDraft = AlarmDraft(),
    val ui: AdvancedAlarmSettingsUiState = AdvancedAlarmSettingsUiState()
)

data class AdvancedAlarmSettingsUiState(
    val isChooseGentleWakeUpDurationDialogVisible: Boolean = false,
    val isChooseTemporaryMuteDurationDialogVisible: Boolean = false,
    val isChooseCancelLockDurationDialogVisible: Boolean = false,
    val availableGentleWakeUpDurationsInSeconds: List<Int> =
        AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS,
    val availableTemporaryMuteDurationsInSeconds: List<Int> =
        AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS,
    val availableCancelLockDurationsInMinutes: List<Int> =
        AVAILABLE_CANCEL_LOCK_DURATIONS_IN_MINUTES
)
