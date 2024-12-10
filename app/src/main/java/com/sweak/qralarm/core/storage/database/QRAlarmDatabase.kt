package com.sweak.qralarm.core.storage.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class QRAlarmDatabase : RoomDatabase() {
    abstract fun alarmsDao(): AlarmsDao
}