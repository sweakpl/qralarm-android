package com.sweak.qralarm.features.add_edit_alarm

import android.net.Uri
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_SNOOZE_NUMBERS
import com.sweak.qralarm.core.domain.alarm.AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper

data class AddEditAlarmScreenState(
    val isLoading: Boolean = true,
    val isEditingExistingAlarm: Boolean = false,
    val alarmHourOfDay: Int? = null,
    val alarmMinute: Int? = null,
    val alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper =
        AlarmRepeatingScheduleWrapper(),
    val isChooseAlarmRepeatingScheduleDialogVisible: Boolean = false,
    val alarmSnoozeMode: Alarm.SnoozeMode = Alarm.SnoozeMode(3, 10),
    val availableSnoozeNumbers: List<Int> = AVAILABLE_SNOOZE_NUMBERS,
    val availableSnoozeDurationsInMinutes: List<Int> = AVAILABLE_SNOOZE_DURATIONS_IN_MINUTES,
    val isChooseAlarmSnoozeConfigurationDialogVisible: Boolean = false,
    val ringtone: Ringtone = Ringtone.GENTLE_GUITAR,
    val availableRingtonesWithPlaybackState: Map<Ringtone, Boolean> =
        Ringtone.entries.associateWith { false },
    val currentCustomAlarmRingtoneUri: Uri? = null,
    val temporaryCustomAlarmRingtoneUri: Uri? = null,
    val isChooseAlarmRingtoneDialogVisible: Boolean = false,
    val areVibrationsEnabled: Boolean = true,
    val isCodeEnabled: Boolean = true,
    val isAssignCodeDialogVisible: Boolean = false,
    val previouslySavedCodes: List<String> = emptyList(),
    val isCameraPermissionDeniedDialogVisible: Boolean = false,
    val isNotificationsPermissionDeniedDialogVisible: Boolean = false,
    val currentlyAssignedCode: String? = null,
    val temporaryAssignedCode: String? = null,
    val isOpenCodeLinkEnabled: Boolean = false,
    val isOneHourLockEnabled: Boolean = true,
    val alarmLabel: String? = null,
    val gentleWakeupDurationInSeconds: Int = 30,
    val availableGentleWakeUpDurationsInSeconds: List<Int> =
        AVAILABLE_GENTLE_WAKE_UP_DURATIONS_IN_SECONDS,
    val isChooseGentleWakeUpDurationDialogVisible: Boolean = false,
    val temporaryMuteDurationInSeconds: Int = 15,
    val availableTemporaryMuteDurationsInSeconds: List<Int> =
        AVAILABLE_TEMPORARY_MUTE_DURATIONS_IN_SECONDS,
    val isChooseTemporaryMuteDurationDialogVisible: Boolean = false,
    val permissionsDialogState: PermissionsDialogState = PermissionsDialogState(),
    val isDeleteAlarmDialogVisible: Boolean = false,
    val isDiscardAlarmChangesDialogVisible: Boolean = false
) {
    data class PermissionsDialogState(
        val isVisible: Boolean = false,
        val cameraPermissionState: Boolean? = null,
        val notificationsPermissionState: Boolean? = null,
        val alarmsPermissionState: Boolean? = null,
        val fullScreenIntentPermissionState: Boolean? = null
    )
}