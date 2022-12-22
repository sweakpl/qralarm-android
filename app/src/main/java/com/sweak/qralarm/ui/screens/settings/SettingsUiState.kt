package com.sweak.qralarm.ui.screens.settings

import com.sweak.qralarm.util.*

data class SettingsUiState(
    val availableAlarmSounds: List<AlarmSound> = AlarmSound.values().toList(),
    val selectedAlarmSoundIndex: Int,
    val alarmPreviewPlaying: Boolean = false,
    val alarmSoundsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeDurations: List<SnoozeDuration> = SnoozeDuration.values().toList(),
    val selectedSnoozeDurationIndex: Int,
    val snoozeDurationsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeMaxCounts: List<SnoozeMaxCount> = SnoozeMaxCount.values().toList(),
    val selectedSnoozeMaxCountIndex: Int,
    val snoozeMaxCountsDropdownMenuExpanded: Boolean = false,
    val availableGentleWakeupDurations: List<GentleWakeupDuration> =
        GentleWakeupDuration.values().toList(),
    val selectedGentleWakeupDurationIndex: Int,
    val availableGentleWakeupDurationsDropdownMenuExpanded: Boolean = false,
    val showStoragePermissionDialog: Boolean = false,
    val showStoragePermissionRevokedDialog: Boolean = false,
    val dismissAlarmCode: String,
    val showCameraPermissionDialog: Boolean = false,
    val showCameraPermissionRevokedDialog: Boolean = false,
    val showDismissCodeAddedDialog: Boolean = false,
    val easyDismissWithoutAlarm: Boolean = false
)
