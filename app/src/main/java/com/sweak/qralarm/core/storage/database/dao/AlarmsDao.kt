package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.sweak.qralarm.core.storage.database.model.AlarmEntity

@Dao
interface AlarmsDao {

    @Upsert
    suspend fun upsertAlarm(alarmEntity: AlarmEntity)
}