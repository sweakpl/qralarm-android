package com.sweak.qralarm.ui.screens.settings

import com.sweak.qralarm.util.AVAILABLE_SNOOZE_MAX_COUNTS
import com.sweak.qralarm.util.AVAILABLE_SNOOZE_DURATIONS

data class SettingsUiState(
    val availableSnoozeDurations: List<Int> = AVAILABLE_SNOOZE_DURATIONS,
    val selectedSnoozeDurationIndex: Int,
    val snoozeDurationsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeMaxCounts: List<Int> = AVAILABLE_SNOOZE_MAX_COUNTS,
    val selectedSnoozeMaxCountIndex: Int,
    val snoozeMaxCountsDropdownMenuExpanded: Boolean = false
)
