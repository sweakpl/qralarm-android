package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.ui.getHourAndMinuteOfAlarmTimeInMillis
import java.time.ZonedDateTime
import javax.inject.Inject

class SnoozeAlarm @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val alarmsRepository: AlarmsRepository
) {
    suspend operator fun invoke(alarmId: Long, isReschedulingCurrentOrMissedSnooze: Boolean) {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return
        val snoozeAlarmTimeInMillis =
            if (isReschedulingCurrentOrMissedSnooze) alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis
            else ZonedDateTime.now()
                .withSecond(0)
                .withNano(0)
                .plusMinutes(alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes.toLong())
                .toInstant()
                .toEpochMilli()

        alarmsRepository.addOrEditAlarm(
            alarm = alarm.copy(
                snoozeConfig = alarm.snoozeConfig.copy(
                    numberOfSnoozesLeft =
                    if (isReschedulingCurrentOrMissedSnooze) alarm.snoozeConfig.numberOfSnoozesLeft
                    else alarm.snoozeConfig.numberOfSnoozesLeft - 1,
                    isAlarmSnoozed = true,
                    nextSnoozedAlarmTimeInMillis = snoozeAlarmTimeInMillis
                )
            )
        )

        if (snoozeAlarmTimeInMillis != null) {
            qrAlarmManager.setAlarm(
                alarmId = alarm.alarmId,
                alarmTimeInMillis = snoozeAlarmTimeInMillis,
                isSnoozeAlarm = true
            )

            val (alarmHourOfDay, alarmMinute) =
                getHourAndMinuteOfAlarmTimeInMillis(snoozeAlarmTimeInMillis)

            qrAlarmManager.showUpcomingAlarmNotification(
                alarmId = alarm.alarmId,
                alarmHourOfDay = alarmHourOfDay,
                alarmMinute = alarmMinute,
                isSnoozeAlarm = true
            )
        }
    }
}