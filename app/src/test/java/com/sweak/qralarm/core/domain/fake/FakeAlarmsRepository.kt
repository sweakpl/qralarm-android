package com.sweak.qralarm.core.domain.fake

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAlarmsRepository : AlarmsRepository {

    private val alarms = mutableMapOf<Long, Alarm>()
    private var lastAlarmId = 0L

    override suspend fun addOrEditAlarm(alarm: Alarm): Long {
        val alarmId = if (alarm.alarmId == 0L) lastAlarmId + 1 else alarm.alarmId
        lastAlarmId = maxOf(lastAlarmId, alarmId)

        alarms[alarmId] = alarm.copy(alarmId = alarmId)

        return alarmId
    }

    override suspend fun getAlarm(alarmId: Long): Alarm? = alarms[alarmId]

    override suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean) = TODO()
    override suspend fun setAlarmRunning(alarmId: Long, running: Boolean) = TODO()
    override suspend fun setAlarmSnoozed(alarmId: Long, snoozed: Boolean) = TODO()
    override suspend fun setSkipNextAlarm(alarmId: Long, skip: Boolean) = TODO()
    override fun getAlarmFlow(alarmId: Long): Flow<Alarm> = TODO()
    override fun getAllAlarms(): Flow<List<Alarm>> = flow { emit(alarms.values.toList()) }
    override suspend fun deleteAlarm(alarmId: Long) {
        alarms.remove(alarmId)
    }

    override suspend fun setAvailableSnoozes(alarmId: Long, availableSnoozes: Int) = TODO()
    override suspend fun setAlarmRingtoneUri(alarmId: Long, uri: String?) {
        alarms.computeIfPresent(alarmId) { _, alarm -> alarm.copy(customRingtoneUriString = uri) }
    }

    override suspend fun setAlarmRingtone(alarmId: Long, ringtone: Alarm.Ringtone) {
        alarms.computeIfPresent(alarmId) { _, alarm -> alarm.copy(ringtone = ringtone) }
    }
}
