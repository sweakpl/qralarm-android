package com.sweak.qralarm.core.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = true
)
abstract class QRAlarmDatabase : RoomDatabase() {
    abstract fun alarmsDao(): AlarmsDao
}