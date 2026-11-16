package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit

import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_NUMBERS
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.core.ui.model.PermissionsDialogState
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft

data class AddEditAlarmState(
    val draft: AlarmDraft = AlarmDraft(),
    val previouslySavedCodes: List<Code> = emptyList(),
    val playingRingtonePreview: Ringtone? = null,
    val ui: AddEditAlarmUiState = AddEditAlarmUiState()
) {
    val availableRingtonesWithPlaybackState: Map<Ringtone, Boolean>
        get() = ui.availableRingtones.associateWith { it == playingRingtonePreview }
}

data class AddEditAlarmUiState(
    val isLoading: Boolean = true,
    val isEditingExistingAlarm: Boolean = false,
    val isDialerPickerDialogVisible: Boolean = false,
    val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int> = Triple(0, 0, 0),
    val isDatePickerDialogVisible: Boolean = false,
    val isChooseAlarmRepeatingScheduleDialogVisible: Boolean = false,
    val availableSnoozeNumbers: List<Int> = AVAILABLE_SNOOZE_NUMBERS,
    val availableSnoozeDurationsInMinutes: List<Int> = AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES,
    val isChooseAlarmSnoozeConfigurationDialogVisible: Boolean = false,
    val availableRingtones: List<Ringtone> = emptyList(),
    val isChooseAlarmRingtoneConfigDialogVisible: Boolean = false,
    val isAssignCodeDialogVisible: Boolean = false,
    val editCodeNameDialogState: EditCodeNameDialogState = EditCodeNameDialogState(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isDeleteAlarmDialogVisible: Boolean = false,
    val isDiscardAlarmChangesDialogVisible: Boolean = false,
    val isDownloadCodeDialogVisible: Boolean = false
) {
    data class EditCodeNameDialogState(
        val isVisible: Boolean = false,
        val initialCodeName: String? = null
    )
}
