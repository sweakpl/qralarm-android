package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit

import android.net.Uri
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.model.AlarmPermission
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.Code

sealed class AddEditAlarmScreenUserEvent {
    data object OnCancelClicked : AddEditAlarmScreenUserEvent()
    data class DiscardAlarmChangesDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()

    data object ConfirmDiscardAlarmChanges : AddEditAlarmScreenUserEvent()
    data object SaveAlarmClicked : AddEditAlarmScreenUserEvent()
    data class TrySaveAlarm(
        val cameraPermissionStatus: Boolean,
        val notificationsPermissionStatus: Boolean
    ) : AddEditAlarmScreenUserEvent()

    data object HideMissingPermissionsDialog : AddEditAlarmScreenUserEvent()
    data class RequestPermission(
        val permission: AlarmPermission
    ) : AddEditAlarmScreenUserEvent()

    data class NotificationsPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()

    data class DialerPickerDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data class AlarmTimeChanged(
        val newAlarmHourOfDay: Int,
        val newAlarmMinute: Int,
        val source: Source
    ) : AddEditAlarmScreenUserEvent() {
        enum class Source {
            Spinner, Dialog
        }
    }

    data class DatePickerDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data class AlarmDateSelected(val selectedDateInMillis: Long) : AddEditAlarmScreenUserEvent()
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
        val newSnoozeNumberToDurationPair: Pair<Int, Int>
    ) : AddEditAlarmScreenUserEvent()

    data class ChooseAlarmRingtoneDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()

    data class AlarmRingtoneConfigSelected(
        val newRingtone: Ringtone,
        val newAlarmVolumePercentage: Int?
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
    data class AssignCodeDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data class EditCodeNameDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data object TryScanSpecificCode : AddEditAlarmScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()

    data class CodeChosenFromList(val code: Code) : AddEditAlarmScreenUserEvent()
    data class CodeNameEdited(val newName: String?) : AddEditAlarmScreenUserEvent()
    data object ClearAssignedCode : AddEditAlarmScreenUserEvent()
    data object GoToApplicationSettingsClicked : AddEditAlarmScreenUserEvent()
    data class AlarmLabelChanged(val newAlarmLabel: String) : AddEditAlarmScreenUserEvent()
    data object AdvancedSettingsClicked : AddEditAlarmScreenUserEvent()
    data object SpecialSettingsClicked : AddEditAlarmScreenUserEvent()
    data class DeleteAlarmDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data object DeleteAlarm : AddEditAlarmScreenUserEvent()
    data class DownloadCodeDialogVisible(val isVisible: Boolean) : AddEditAlarmScreenUserEvent()
    data object DownloadCode : AddEditAlarmScreenUserEvent()
    data object AlarmsChainSettingsClicked : AddEditAlarmScreenUserEvent()
    data object RefreshAlarmCountdown : AddEditAlarmScreenUserEvent()
}
