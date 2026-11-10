package com.sweak.qralarm.features.add_edit_alarm

import android.os.Parcelable
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_CANCEL_LOCK_DURATIONS_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_NUMBERS
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddEditAlarmFlowState(
    val isLoading: Boolean = true,
    val isEditingExistingAlarm: Boolean = false,
    val draft: AlarmDraft = AlarmDraft(),
    val isDialerPickerDialogVisible: Boolean = false,
    val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int> = Triple(0, 0, 0),
    val isDatePickerDialogVisible: Boolean = false,
    val isChooseAlarmRepeatingScheduleDialogVisible: Boolean = false,
    val availableSnoozeNumbers: List<Int> = AVAILABLE_SNOOZE_NUMBERS,
    val availableSnoozeDurationsInMinutes: List<Int> = AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES,
    val isChooseAlarmSnoozeConfigurationDialogVisible: Boolean = false,
    val availableRingtonesWithPlaybackState: Map<Ringtone, Boolean> = emptyMap(),
    val isChooseAlarmRingtoneConfigDialogVisible: Boolean = false,
    val isAssignCodeDialogVisible: Boolean = false,
    val editCodeNameDialogState: EditCodeNameDialogState = EditCodeNameDialogState(),
    val previouslySavedCodes: List<Code> = emptyList(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
    val availableCancelLockDurationsInMinutes: List<Int> =
        AVAILABLE_CANCEL_LOCK_DURATIONS_IN_MINUTES,
    val isChooseCancelLockDurationDialogVisible: Boolean = false,
    val availableGentleWakeUpDurationsInSeconds: List<Int> =
        AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS,
    val isChooseGentleWakeUpDurationDialogVisible: Boolean = false,
    val availableTemporaryMuteDurationsInSeconds: List<Int> =
        AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS,
    val isChooseTemporaryMuteDurationDialogVisible: Boolean = false,
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isDeleteAlarmDialogVisible: Boolean = false,
    val isDiscardAlarmChangesDialogVisible: Boolean = false,
    val isDownloadCodeDialogVisible: Boolean = false
) : Parcelable {
    @Parcelize
    data class PermissionsDialogState(
        val isVisible: Boolean = false,
        val cameraPermissionState: Boolean? = null,
        val notificationsPermissionState: Boolean? = null,
        val alarmsPermissionState: Boolean? = null,
        val fullScreenIntentPermissionState: Boolean? = null
    ) : Parcelable

    @Parcelize
    data class EditCodeNameDialogState(
        val isVisible: Boolean = false,
        val initialCodeName: String? = null
    ) : Parcelable
}