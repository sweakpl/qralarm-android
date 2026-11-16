package com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain

sealed class AlarmsChainSettingsScreenUserEvent {
    data object OnCancelClicked : AlarmsChainSettingsScreenUserEvent()
    data object AddNewChainedAlarmClicked : AlarmsChainSettingsScreenUserEvent()
}
