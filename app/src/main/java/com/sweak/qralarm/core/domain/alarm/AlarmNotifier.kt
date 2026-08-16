package com.sweak.qralarm.core.domain.alarm

interface AlarmNotifier {
    fun showUpcomingAlarmNotification(
        alarmId: Long,
        alarmHourOfDay: Int,
        alarmMinute: Int,
        isSnoozeAlarm: Boolean
    )

    fun notifyAboutMissedAlarm()

    fun notifyAboutEmergencyDisabledRepeatingAlarm()
}
