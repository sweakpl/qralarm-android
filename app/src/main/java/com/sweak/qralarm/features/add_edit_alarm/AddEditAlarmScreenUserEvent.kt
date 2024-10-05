package com.sweak.qralarm.features.add_edit_alarm

import com.sweak.qralarm.core.domain.alarm.AlarmRingtone
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmSnoozeConfigurationWrapper

sealed class AddEditAlarmScreenUserEvent {
    data object OnCancelClicked : AddEditAlarmScreenUserEvent()
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
        val newAlarmSnoozeConfigurationWrapper: AlarmSnoozeConfigurationWrapper
    ) : AddEditAlarmScreenUserEvent()
    data class ChooseAlarmRingtoneDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class AlarmRingtoneSelected(
        val newAlarmRingtone: AlarmRingtone
    ) : AddEditAlarmScreenUserEvent()
    data class ToggleAlarmRingtonePlayback(
        val alarmRingtone: AlarmRingtone
    ) : AddEditAlarmScreenUserEvent()
    data class VibrationsEnabledChanged(val areEnabled: Boolean) : AddEditAlarmScreenUserEvent()
    data class CodeEnabledChanged(val isEnabled: Boolean) : AddEditAlarmScreenUserEvent()
    data class ChooseGentleWakeUpDurationDialogVisible(
        val isVisible: Boolean
    ) : AddEditAlarmScreenUserEvent()
    data class GentleWakeUpDurationSelected(
        val newGentleWakeUpDurationInSeconds: Int
    ) : AddEditAlarmScreenUserEvent()
    data class TemporaryMuteEnabledChanged(
        val isEnabled: Boolean
    ) : AddEditAlarmScreenUserEvent()
}