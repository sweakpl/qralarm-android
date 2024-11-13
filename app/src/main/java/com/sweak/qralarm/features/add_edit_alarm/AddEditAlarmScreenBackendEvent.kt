package com.sweak.qralarm.features.add_edit_alarm

sealed class AddEditAlarmScreenBackendEvent {
    data object AlarmChangesDiscarded : AddEditAlarmScreenBackendEvent()
    data class AlarmSaved(
        val daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>? = null
    ) : AddEditAlarmScreenBackendEvent()
    data class CustomRingtoneRetrievalFinished(
        val isSuccess: Boolean
    ) : AddEditAlarmScreenBackendEvent()
    data object CustomCodeAssignmentFinished : AddEditAlarmScreenBackendEvent()
    data object AlarmDeleted : AddEditAlarmScreenBackendEvent()
    data object AlarmRingtonePreviewPlaybackError : AddEditAlarmScreenBackendEvent()
}