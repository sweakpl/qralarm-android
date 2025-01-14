package com.sweak.qralarm.core.data.alarm

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
                isAlarmRunning = alarm.isAlarmRunning,
                nextAlarmTimeInMillis = alarm.nextAlarmTimeInMillis,
                repeatingAlarmDays =
                if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                    alarm.repeatingMode.repeatingDaysOfWeek.joinToString { it.name }
                } else null,
                numberOfSnoozes = alarm.snoozeConfig.snoozeMode.numberOfSnoozes,
                snoozeDurationInMinutes = alarm.snoozeConfig.snoozeMode.snoozeDurationInMinutes,
                numberOfSnoozesLeft = alarm.snoozeConfig.numberOfSnoozesLeft,
                isAlarmSnoozed = alarm.snoozeConfig.isAlarmSnoozed,
                nextSnoozedAlarmTimeInMillis = alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis,
                ringtone = alarm.ringtone.name,
                customRingtoneUriString = alarm.customRingtoneUriString,
                areVibrationsEnabled = alarm.areVibrationsEnabled,
                isUsingCode = alarm.isUsingCode,
                assignedCode = alarm.assignedCode,
                isOpenCodeLinkEnabled = alarm.isOpenCodeLinkEnabled,
                isOneHourLockEnabled = alarm.isOneHourLockEnabled,
                alarmLabel = alarm.alarmLabel,
                gentleWakeUpDurationInSeconds = alarm.gentleWakeUpDurationInSeconds,
                temporaryMuteDurationInSeconds = alarm.temporaryMuteDurationInSeconds,
                skipAlarmUntilTimeInMillis = alarm.skipAlarmUntilTimeInMillis
            )
        )
    }

    override suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean) {
        alarmsDao.getAlarm(alarmId = alarmId).firstOrNull()?.let { alarmEntity ->
            alarmsDao.upsertAlarm(
                alarmEntity = alarmEntity.copy(isAlarmEnabled = enabled)
            )
        }
    }

    override suspend fun setAlarmRunning(alarmId: Long, running: Boolean) {
        alarmsDao.getAlarm(alarmId = alarmId).firstOrNull()?.let { alarmEntity ->
            alarmsDao.upsertAlarm(
                alarmEntity = alarmEntity.copy(isAlarmRunning = running)
            )
        }
    }

    override suspend fun setAlarmSnoozed(alarmId: Long, snoozed: Boolean) {
        alarmsDao.getAlarm(alarmId = alarmId).firstOrNull()?.let { alarmEntity ->
            alarmsDao.upsertAlarm(
                alarmEntity = alarmEntity.copy(
                    isAlarmSnoozed = snoozed,
                    nextSnoozedAlarmTimeInMillis =
                    if (!snoozed) null else alarmEntity.nextSnoozedAlarmTimeInMillis
                )
            )
        }
    }

    override suspend fun setSkipNextAlarm(alarmId: Long, skip: Boolean) {
        alarmsDao.getAlarm(alarmId = alarmId).firstOrNull()?.let { alarmEntity ->
            alarmsDao.upsertAlarm(
                alarmEntity = alarmEntity.copy(
                    skipAlarmUntilTimeInMillis =
                    if (skip) alarmEntity.nextAlarmTimeInMillis else null
                )
            )
        }
    }

    override suspend fun getAlarm(alarmId: Long): Alarm? {
        return alarmsDao.getAlarm(alarmId = alarmId).firstOrNull()?.let { alarmEntity ->
            convertAlarmEntity(alarmEntity = alarmEntity)
        }
    }

    override fun getAlarmFlow(alarmId: Long): Flow<Alarm> {
        return alarmsDao.getAlarm(alarmId = alarmId).mapNotNull { alarmEntity ->
            alarmEntity?.let { convertAlarmEntity(alarmEntity = it) }
        }
    }

    override fun getAllAlarms(): Flow<List<Alarm>> {
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
            isAlarmRunning = alarmEntity.isAlarmRunning,
            repeatingMode = repeatingMode,
            nextAlarmTimeInMillis = alarmEntity.nextAlarmTimeInMillis,
            snoozeConfig = Alarm.SnoozeConfig(
                snoozeMode = Alarm.SnoozeMode(
                    numberOfSnoozes = alarmEntity.numberOfSnoozes,
                    snoozeDurationInMinutes = alarmEntity.snoozeDurationInMinutes
                ),
                numberOfSnoozesLeft = alarmEntity.numberOfSnoozesLeft,
                isAlarmSnoozed = alarmEntity.isAlarmSnoozed,
                nextSnoozedAlarmTimeInMillis = alarmEntity.nextSnoozedAlarmTimeInMillis
            ),
            ringtone = Alarm.Ringtone.valueOf(alarmEntity.ringtone),
            customRingtoneUriString = alarmEntity.customRingtoneUriString,
            areVibrationsEnabled = alarmEntity.areVibrationsEnabled,
            isUsingCode = alarmEntity.isUsingCode,
            assignedCode = alarmEntity.assignedCode,
            isOpenCodeLinkEnabled = alarmEntity.isOpenCodeLinkEnabled,
            isOneHourLockEnabled = alarmEntity.isOneHourLockEnabled,
            alarmLabel = alarmEntity.alarmLabel,
            gentleWakeUpDurationInSeconds = alarmEntity.gentleWakeUpDurationInSeconds,
            temporaryMuteDurationInSeconds = alarmEntity.temporaryMuteDurationInSeconds,
            skipAlarmUntilTimeInMillis = alarmEntity.skipAlarmUntilTimeInMillis
        )
    }
}
