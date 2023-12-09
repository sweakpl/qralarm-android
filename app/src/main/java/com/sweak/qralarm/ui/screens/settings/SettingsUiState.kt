package com.sweak.qralarm.ui.screens.settings

import com.sweak.qralarm.util.*

data class SettingsUiState(
    val availableAlarmSounds: List<AlarmSound> = AlarmSound.entries,
    val selectedAlarmSoundIndex: Int,
    val alarmPreviewPlaying: Boolean = false,
    val alarmSoundsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeDurations: List<SnoozeDuration> = SnoozeDuration.entries,
    val selectedSnoozeDurationIndex: Int,
    val snoozeDurationsDropdownMenuExpanded: Boolean = false,
    val availableSnoozeMaxCounts: List<SnoozeMaxCount> = SnoozeMaxCount.entries,
    val selectedSnoozeMaxCountIndex: Int,
    val snoozeMaxCountsDropdownMenuExpanded: Boolean = false,
    val availableGentleWakeupDurations: List<GentleWakeupDuration> =
        GentleWakeupDuration.entries,
    val selectedGentleWakeupDurationIndex: Int,
    val availableGentleWakeupDurationsDropdownMenuExpanded: Boolean = false,
    val showStoragePermissionDialog: Boolean = false,
    val showStoragePermissionRevokedDialog: Boolean = false,
    val dismissAlarmCode: String,
    val showCameraPermissionDialog: Boolean = false,
    val showCameraPermissionRevokedDialog: Boolean = false,
    val showDismissCodeAddedDialog: Boolean = false,
    val vibrationsEnabled: Boolean,
    val acceptAnyCodeType: Boolean,
    val showDisablingBarcodesSupportDialog: Boolean = false,
    val noCodeCancelEnabled: Boolean
)
