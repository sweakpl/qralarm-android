package com.sweak.qralarm.features.add_edit_alarm.destinations.advanced

sealed class AdvancedAlarmSettingsScreenUserEvent {
    data object OnCancelClicked : AdvancedAlarmSettingsScreenUserEvent()
    data class ChooseGentleWakeUpDurationDialogVisible(
        val isVisible: Boolean
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class GentleWakeUpDurationSelected(
        val newGentleWakeUpDurationInSeconds: Int
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class ChooseTemporaryMuteDurationDialogVisible(
        val isVisible: Boolean
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class TemporaryMuteDurationSelected(
        val newTemporaryMuteDurationInSeconds: Int
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class OpenCodeLinkEnabledChanged(
        val isEnabled: Boolean
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class ChooseCancelLockDurationDialogVisible(
        val isVisible: Boolean
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class CancelLockDurationSelected(
        val newCancelLockDurationInMinutes: Int
    ) : AdvancedAlarmSettingsScreenUserEvent()

    data class EmergencyTaskEnabledChanged(
        val isEnabled: Boolean
    ) : AdvancedAlarmSettingsScreenUserEvent()
}
