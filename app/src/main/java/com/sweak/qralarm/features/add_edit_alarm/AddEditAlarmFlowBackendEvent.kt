package com.sweak.qralarm.features.add_edit_alarm

sealed class AddEditAlarmFlowBackendEvent {
    data object AlarmChangesDiscarded : AddEditAlarmFlowBackendEvent()
    data object AlarmSaved : AddEditAlarmFlowBackendEvent()
    data class CustomRingtoneRetrievalFinished(
        val isSuccess: Boolean
    ) : AddEditAlarmFlowBackendEvent()
    data object CustomCodeAssignmentFinished : AddEditAlarmFlowBackendEvent()
    data object AlarmDeleted : AddEditAlarmFlowBackendEvent()
    data object AlarmRingtonePreviewPlaybackError : AddEditAlarmFlowBackendEvent()
}