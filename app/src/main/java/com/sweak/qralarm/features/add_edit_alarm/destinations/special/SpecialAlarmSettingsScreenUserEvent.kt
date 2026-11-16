package com.sweak.qralarm.features.add_edit_alarm.destinations.special

sealed class SpecialAlarmSettingsScreenUserEvent {
    data object OnCancelClicked : SpecialAlarmSettingsScreenUserEvent()
    data object TryUseSpecialAlarmSettings : SpecialAlarmSettingsScreenUserEvent()
}
