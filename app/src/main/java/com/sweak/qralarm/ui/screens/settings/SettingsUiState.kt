package com.sweak.qralarm.ui.screens.settings

import com.sweak.qralarm.util.AVAILABLE_ALARM_SOUNDS
import com.sweak.qralarm.util.AVAILABLE_SNOOZE_DURATIONS
import com.sweak.qralarm.util.AVAILABLE_SNOOZE_MAX_COUNTS

data class SettingsUiState(
    val availableAlarmSounds: List<String> = AVAILABLE_ALARM_SOUNDS.map { it.toString() },
    val selectedAlarmSoundIndex: Int,
    val alarmSoundsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeDurations: List<Int> = AVAILABLE_SNOOZE_DURATIONS,
    val selectedSnoozeDurationIndex: Int,
    val snoozeDurationsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeMaxCounts: List<Int> = AVAILABLE_SNOOZE_MAX_COUNTS,
    val selectedSnoozeMaxCountIndex: Int,
    val snoozeMaxCountsDropdownMenuExpanded: Boolean = false,
    val showStoragePermissionDialog: Boolean = false,
    val showStoragePermissionRevokedDialog: Boolean = false,
    val showDismissCodeAddedDialog: Boolean = false,
    val dismissAlarmCode: String
)
