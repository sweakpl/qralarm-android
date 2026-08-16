package com.sweak.qralarm.core.domain.alarm

interface AlarmScheduler {
    fun setAlarm(alarmId: Long, alarmTimeInMillis: Long, isSnoozeAlarm: Boolean)

    fun cancelAlarm(alarmId: Long)

    fun scheduleUpcomingAlarmNotification(
        alarmId: Long,
        upcomingAlarmNotificationTimeInMillis: Long
    )

    fun cancelUpcomingAlarmNotification(alarmId: Long)

    fun canScheduleExactAlarms(): Boolean

    fun canUseFullScreenIntent(): Boolean
}
