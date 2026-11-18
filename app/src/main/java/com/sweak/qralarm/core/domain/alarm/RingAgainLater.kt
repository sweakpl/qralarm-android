package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

/**
 * Makes an alarm ring once more at [timeInMillis], after it was dismissed with the emergency task.
 * It goes through the snooze machinery (so it survives reboots and rings even if a one-time alarm
 * was just disabled) but does not use up any of the alarm's snoozes.
 */
class RingAgainLater @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val alarmNotifier: AlarmNotifier,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long, timeInMillis: Long): Boolean {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return false
        if (timeInMillis <= System.currentTimeMillis()) return false

        alarmsRepository.addOrEditAlarm(
            alarm = alarm.copy(
                snoozeConfig = alarm.snoozeConfig.copy(
                    isAlarmSnoozed = true,
                    nextSnoozedAlarmTimeInMillis = timeInMillis
                )
            )
        )
        alarmScheduler.setAlarm(alarmId = alarmId, alarmTimeInMillis = timeInMillis, isSnoozeAlarm = true)

        val (hourOfDay, minute) = getHourAndMinuteOfAlarmTimeInMillis(timeInMillis)
        alarmNotifier.showUpcomingAlarmNotification(
            alarmId = alarmId,
            alarmHourOfDay = hourOfDay,
            alarmMinute = minute,
            isSnoozeAlarm = true
        )
        return true
    }

    companion object {
        val QUICK_DELAYS_IN_MINUTES = listOf(10, 15, 30, 60)

        /** Next occurrence of [hourOfDay]:[minute] after [nowMillis], in the device time zone. */
        fun nextOccurrence(hourOfDay: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
            val zone = java.time.ZoneId.systemDefault()
            val now = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone)
            var candidate = now.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0)
            if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
            return candidate.toInstant().toEpochMilli()
        }

        fun afterMinutes(minutes: Int, nowMillis: Long = System.currentTimeMillis()): Long {
            val zone = java.time.ZoneId.systemDefault()
            return java.time.Instant.ofEpochMilli(nowMillis).atZone(zone)
                .withSecond(0).withNano(0).plusMinutes(minutes.toLong()).toInstant().toEpochMilli()
        }
    }
}
