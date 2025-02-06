package com.sweak.qralarm.features.add_edit_alarm

sealed class AddEditAlarmFlowBackendEvent {
    data object AlarmChangesDiscarded : AddEditAlarmFlowBackendEvent()
    data class AlarmSaved(
        val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>? = null
    ) : AddEditAlarmFlowBackendEvent()
    data class CustomRingtoneRetrievalFinished(
        val isSuccess: Boolean
    ) : AddEditAlarmFlowBackendEvent()
    data object CustomCodeAssignmentFinished : AddEditAlarmFlowBackendEvent()
    data object AlarmDeleted : AddEditAlarmFlowBackendEvent()
    data object AlarmRingtonePreviewPlaybackError : AddEditAlarmFlowBackendEvent()
}