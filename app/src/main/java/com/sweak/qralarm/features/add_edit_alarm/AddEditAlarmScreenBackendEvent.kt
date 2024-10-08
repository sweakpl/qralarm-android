package com.sweak.qralarm.features.add_edit_alarm

sealed class AddEditAlarmScreenBackendEvent {
    data class CustomRingtoneRetrievalFinished(
        val isSuccess: Boolean
    ) : AddEditAlarmScreenBackendEvent()
    data object CustomCodeAssignmentFinished : AddEditAlarmScreenBackendEvent()
}