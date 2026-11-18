package com.sweak.qralarm.core.storage.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomObjectMigrationTest {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), QRAlarmDatabase::class.java
    )

    @Test fun keepsAlarmsAndSupportsSeveralItemsPerAlarm() {
        helper.createDatabase("custom-object-migration", 11).use { db ->
            db.execSQL("INSERT INTO code (codeId, value, name) VALUES (1, 'test-code', 'Bathroom')")
            db.execSQL("""
                INSERT INTO alarm (
                    alarmHourOfDay, alarmMinute, isAlarmEnabled, isAlarmRunning, nextAlarmTimeInMillis,
                    numberOfSnoozes, snoozeDurationInMinutes, numberOfSnoozesLeft, isAlarmSnoozed, ringtone,
                    areVibrationsEnabled, dismissalMethod, objectCategoryId, assignedCodeId,
                    gentleWakeUpDurationInSeconds, temporaryMuteDurationInSeconds
                ) VALUES (7, 30, 1, 0, 123456, 2, 5, 2, 0, 'GENTLE_GUITAR', 1, 'OBJECT', 'sink', 1, 10, 15)
            """.trimIndent())
        }
        helper.runMigrationsAndValidate("custom-object-migration", 12, true, QRAlarmDatabase.MIGRATION_11_12).use { db ->
            db.query("SELECT dismissalMethod, objectCategoryId, assignedCodeId FROM alarm").use {
                assertTrue(it.moveToFirst())
                assertEquals("OBJECT", it.getString(0))
                assertEquals("sink", it.getString(1))
                assertEquals(1L, it.getLong(2))
            }
            db.execSQL("INSERT INTO custom_object (objectId, name, createdAtMillis, referenceCount, embeddingThreshold) VALUES (5, 'Pills', 0, 3, 0.2)")
            db.execSQL("INSERT INTO custom_object (objectId, name, createdAtMillis, referenceCount, embeddingThreshold) VALUES (6, 'Face wash', 0, 3, 0.2)")
            db.execSQL("UPDATE alarm SET dismissalMethod = 'CUSTOM_OBJECT'")
            db.execSQL("INSERT INTO alarm_custom_object (alarmId, objectId, position) VALUES (1, 5, 0), (1, 6, 1)")
            db.execSQL("INSERT INTO emergency_explanation (alarmId, createdAtMillis, text) VALUES (1, 0, 'text')")
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("DELETE FROM custom_object WHERE objectId = 5")
            // Deleting one of two items leaves the other one linked to the alarm.
            db.query("SELECT objectId FROM alarm_custom_object WHERE alarmId = 1").use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                assertEquals(6L, it.getLong(0))
            }
        }
    }
}
