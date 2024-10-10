package com.sweak.qralarm.features.add_edit_alarm

import android.net.Uri
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper

sealed class AddEditAlarmScreenUserEvent {
    data object OnCancelClicked : AddEditAlarmScreenUserEvent()
    data object SaveAlarmClicked : AddEditAlarmScreenUserEvent()
    data class TrySaveAlarm(
        val cameraPermissionStatus: Boolean,
        val notificationsPermissionStatus: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data object HideMissingPermissionsDialog : AddEditAlarmScreenUserEvent()
    data object RequestCameraPermission : AddEditAlarmScreenUserEvent()
    data object RequestNotificationsPermission : AddEditAlarmScreenUserEvent()
    data object RequestAlarmsPermission : AddEditAlarmScreenUserEvent()
    data object RequestFullScreenIntentPermission : AddEditAlarmScreenUserEvent()
    data class NotificationsPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmTimeChanged(
        val newAlarmHourOfDay: Int,
        val newAlarmMinute: Int
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmEnabledChanged(val isEnabled: Boolean) : AddEditAlarmScreenUserEvent()
    data class ChooseAlarmRepeatingScheduleDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmRepeatingScheduleSelected(
        val newAlarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper
    ) : AddEditAlarmScreenUserEvent()
    data class ChooseAlarmSnoozeConfigurationDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmSnoozeConfigurationSelected(
        val newAlarmSnoozeMode: Alarm.SnoozeMode
    ) : AddEditAlarmScreenUserEvent()
    data class ChooseAlarmRingtoneDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmRingtoneSelected(
        val newRingtone: Ringtone
    ) : AddEditAlarmScreenUserEvent()
    data class ToggleAlarmRingtonePlayback(
        val ringtone: Ringtone
    ) : AddEditAlarmScreenUserEvent()
    data object PickCustomRingtone : AddEditAlarmScreenUserEvent()
    data class CustomRingtoneUriRetrieved(
        val customRingtoneUri: Uri?
    ) : AddEditAlarmScreenUserEvent()
    data class VibrationsEnabledChanged(val areEnabled: Boolean) : AddEditAlarmScreenUserEvent()
    data class CodeEnabledChanged(val isEnabled: Boolean) : AddEditAlarmScreenUserEvent()
    data object TryScanSpecificCode : AddEditAlarmScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data object ClearAssignedCode : AddEditAlarmScreenUserEvent()
    data object GoToApplicationSettingsClicked : AddEditAlarmScreenUserEvent()
    data class ChooseGentleWakeUpDurationDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class GentleWakeUpDurationSelected(
        val newGentleWakeUpDurationInSeconds: Int
    ) : AddEditAlarmScreenUserEvent()
    data class TemporaryMuteEnabledChanged(val isEnabled: Boolean) : AddEditAlarmScreenUserEvent()
}