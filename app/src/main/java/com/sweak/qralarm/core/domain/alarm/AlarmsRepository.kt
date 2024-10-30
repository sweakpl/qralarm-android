package com.sweak.qralarm.core.domain.alarm

import kotlinx.coroutines.flow.Flow

interface AlarmsRepository {
    suspend fun addOrEditAlarm(alarm: Alarm): Long
    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean)
    suspend fun setAlarmRunning(alarmId: Long, running: Boolean)
    suspend fun setAlarmSnoozed(alarmId: Long, snoozed: Boolean)
    suspend fun setSkipNextAlarm(alarmId: Long, skip: Boolean)
    suspend fun getAlarm(alarmId: Long): Alarm?
    fun getAlarmFlow(alarmId: Long): Flow<Alarm>
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun deleteAlarm(alarmId: Long)
}