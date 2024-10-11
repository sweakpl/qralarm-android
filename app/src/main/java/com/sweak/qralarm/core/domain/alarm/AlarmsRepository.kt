package com.sweak.qralarm.core.domain.alarm

interface AlarmsRepository {
    suspend fun addOrEditAlarm(alarm: Alarm)
    suspend fun getAlarm(alarmId: Long): Alarm?
    suspend fun getAllAlarms(): List<Alarm>
}