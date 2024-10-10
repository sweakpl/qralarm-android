package com.sweak.qralarm.core.data

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import javax.inject.Inject

class AlarmsRepositoryImpl @Inject constructor(
    private val alarmsDao: AlarmsDao
) : AlarmsRepository {

    override suspend fun addOrEditAlarm(alarm: Alarm) {
        alarmsDao.upsertAlarm(
            alarmEntity = AlarmEntity(
                alarmHourOfDay = alarm.alarmHourOfDay,
                alarmMinute = alarm.alarmMinute,
                isAlarmEnabled = alarm.isAlarmEnabled,
                repeatingAlarmOnceDayInMillis =
                if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                    alarm.repeatingMode.alarmDayInMillis
                } else null,
                repeatingAlarmDays =
                if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                    alarm.repeatingMode.repeatingDaysOfWeek.joinToString { it.name }
                } else null,
                numberOfSnoozes = alarm.snoozeMode.numberOfSnoozes,
                snoozeDurationInMinutes = alarm.snoozeMode.snoozeDurationInMinutes,
                ringtone = alarm.ringtone.name,
                customRingtoneUriString = alarm.customRingtoneUriString,
                areVibrationsEnabled = alarm.areVibrationsEnabled,
                isUsingCode = alarm.isUsingCode,
                assignedCode = alarm.assignedCode,
                gentleWakeUpDurationInSeconds = alarm.gentleWakeUpDurationInSeconds,
                isTemporaryMuteEnabled = alarm.isTemporaryMuteEnabled
            )
        )
    }
}