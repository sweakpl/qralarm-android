package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sweak.qralarm.core.storage.database.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmsDao {

    @Upsert
    suspend fun upsertAlarm(alarmEntity: AlarmEntity)

    @Query("SELECT * FROM alarm")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarm WHERE alarmId = :alarmId")
    suspend fun getAlarm(alarmId: Long): AlarmEntity?
}