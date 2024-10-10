package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sweak.qralarm.core.storage.database.model.AlarmEntity

@Dao
interface AlarmsDao {

    @Upsert
    suspend fun upsertAlarm(alarmEntity: AlarmEntity)

    @Query("SELECT * FROM alarm")
    suspend fun getAllAlarms(): List<AlarmEntity>
}