package com.sweak.qralarm.features.emergency.task

sealed class EmergencyScreenBackendEvent {
    /** Keep the ringing alarm silent; repeated while the user is working on the task. */
    data object HoldAlarmSilent : EmergencyScreenBackendEvent()
    /** Let the alarm ring again: the user went idle or left. */
    data object ReleaseAlarm : EmergencyScreenBackendEvent()
    data object EmergencyTaskCompleted : EmergencyScreenBackendEvent()
    data object RingAgainScheduled : EmergencyScreenBackendEvent()
}
