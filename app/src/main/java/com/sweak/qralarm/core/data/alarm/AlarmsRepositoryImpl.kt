package com.sweak.qralarm.core.data.alarm

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject

class AlarmsRepositoryImpl @Inject constructor(
    private val alarmsDao: AlarmsDao
) : AlarmsRepository {

    override suspend fun addOrEditAlarm(alarm: Alarm): Long {
        return alarmsDao.upsertAlarm(
            alarmEntity = AlarmEntity(
                alarmId = alarm.alarmId,
                alarmHourOfDay = alarm.alarmHourOfDay,
                alarmMinute = alarm.alarmMinute,
                isAlarmEnabled = alarm.isAlarmEnabled,
                nextAlarmTimeInMillis = alarm.nextAlarmTimeInMillis,
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

    override suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean) {
        alarmsDao.getAlarm(alarmId = alarmId)?.let { alarmEntity ->
            alarmsDao.upsertAlarm(
                alarmEntity = alarmEntity.copy(isAlarmEnabled = enabled)
            )
        }
    }

    override suspend fun getAlarm(alarmId: Long): Alarm? {
        return alarmsDao.getAlarm(alarmId = alarmId)?.let { alarmEntity ->
            convertAlarmEntity(alarmEntity = alarmEntity)
        }
    }

    override suspend fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmsDao.getAllAlarms().map { alarmEntityList ->
            alarmEntityList.map { alarmEntity ->
                convertAlarmEntity(alarmEntity = alarmEntity)
            }
        }
    }

    override suspend fun deleteAlarm(alarmId: Long) {
        alarmsDao.deleteAlarm(alarmId = alarmId)
    }

    private fun convertAlarmEntity(alarmEntity: AlarmEntity): Alarm {
        val repeatingMode = if (alarmEntity.repeatingAlarmDays != null) {
            Alarm.RepeatingMode.Days(
                repeatingDaysOfWeek = alarmEntity.repeatingAlarmDays.split(", ").map {
                    DayOfWeek.valueOf(it)
                }
            )
        } else {
            Alarm.RepeatingMode.Once
        }

        return Alarm(
            alarmId = alarmEntity.alarmId,
            alarmHourOfDay = alarmEntity.alarmHourOfDay,
            alarmMinute = alarmEntity.alarmMinute,
            isAlarmEnabled = alarmEntity.isAlarmEnabled,
            repeatingMode = repeatingMode,
            nextAlarmTimeInMillis = alarmEntity.nextAlarmTimeInMillis,
            snoozeMode = Alarm.SnoozeMode(
                numberOfSnoozes = alarmEntity.numberOfSnoozes,
                snoozeDurationInMinutes = alarmEntity.snoozeDurationInMinutes
            ),
            ringtone = Alarm.Ringtone.valueOf(alarmEntity.ringtone),
            customRingtoneUriString = alarmEntity.customRingtoneUriString,
            areVibrationsEnabled = alarmEntity.areVibrationsEnabled,
            isUsingCode = alarmEntity.isUsingCode,
            assignedCode = alarmEntity.assignedCode,
            gentleWakeUpDurationInSeconds = alarmEntity.gentleWakeUpDurationInSeconds,
            isTemporaryMuteEnabled = alarmEntity.isTemporaryMuteEnabled
        )
    }
}
