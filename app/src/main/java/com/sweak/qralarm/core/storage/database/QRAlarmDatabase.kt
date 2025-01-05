package com.sweak.qralarm.core.storage.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.model.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5)
    ]
)
abstract class QRAlarmDatabase : RoomDatabase() {
    abstract fun alarmsDao(): AlarmsDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This migration handles the removal of the column isTemporaryMuteEnabled in favor
                // of the new, more flexible column temporaryMuteDurationInSeconds.

                // Create a new temporary alarm table with the updated schema:
                db.execSQL(
                    """
                    CREATE TABLE alarm_new (
                        alarmId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        alarmHourOfDay INTEGER NOT NULL,
                        alarmMinute INTEGER NOT NULL,
                        isAlarmEnabled INTEGER NOT NULL,
                        isAlarmRunning INTEGER NOT NULL,
                        nextAlarmTimeInMillis INTEGER NOT NULL,
                        repeatingAlarmDays TEXT,
                        numberOfSnoozes INTEGER NOT NULL,
                        snoozeDurationInMinutes INTEGER NOT NULL,
                        numberOfSnoozesLeft INTEGER NOT NULL,
                        isAlarmSnoozed INTEGER NOT NULL,
                        nextSnoozedAlarmTimeInMillis INTEGER,
                        ringtone TEXT NOT NULL,
                        customRingtoneUriString TEXT,
                        areVibrationsEnabled INTEGER NOT NULL,
                        isUsingCode INTEGER NOT NULL,
                        assignedCode TEXT,
                        isOpenCodeLinkEnabled INTEGER NOT NULL DEFAULT FALSE,
                        alarmLabel TEXT,
                        gentleWakeUpDurationInSeconds INTEGER NOT NULL,
                        temporaryMuteDurationInSeconds INTEGER NOT NULL,
                        skipAlarmUntilTimeInMillis INTEGER
                    )
                    """.trimIndent()
                )

                // Migrate data from the old alarm table to the new one:
                db.execSQL(
                    """
                    INSERT INTO alarm_new (
                        alarmId,
                        alarmHourOfDay,
                        alarmMinute,
                        isAlarmEnabled,
                        isAlarmRunning,
                        nextAlarmTimeInMillis,
                        repeatingAlarmDays,
                        numberOfSnoozes,
                        snoozeDurationInMinutes,
                        numberOfSnoozesLeft,
                        isAlarmSnoozed,
                        nextSnoozedAlarmTimeInMillis,
                        ringtone,
                        customRingtoneUriString,
                        areVibrationsEnabled,
                        isUsingCode,
                        assignedCode,
                        isOpenCodeLinkEnabled,
                        alarmLabel,
                        gentleWakeUpDurationInSeconds,
                        temporaryMuteDurationInSeconds,
                        skipAlarmUntilTimeInMillis
                    )
                    SELECT
                        alarmId,
                        alarmHourOfDay,
                        alarmMinute,
                        isAlarmEnabled,
                        isAlarmRunning,
                        nextAlarmTimeInMillis,
                        repeatingAlarmDays,
                        numberOfSnoozes,
                        snoozeDurationInMinutes,
                        numberOfSnoozesLeft,
                        isAlarmSnoozed,
                        nextSnoozedAlarmTimeInMillis,
                        ringtone,
                        customRingtoneUriString,
                        areVibrationsEnabled,
                        isUsingCode,
                        assignedCode,
                        isOpenCodeLinkEnabled,
                        alarmLabel,
                        gentleWakeUpDurationInSeconds,
                        CASE WHEN isTemporaryMuteEnabled = 1 THEN 15 ELSE 0 END AS temporaryMuteDurationInSeconds,
                        skipAlarmUntilTimeInMillis
                    FROM alarm
                    """.trimIndent()
                )

                // Drop the old table and rename the new table to the old table's name:
                db.execSQL("DROP TABLE alarm")
                db.execSQL("ALTER TABLE alarm_new RENAME TO alarm")
            }
        }
    }
}