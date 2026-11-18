package com.sweak.qralarm.core.storage.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Alarm details are copied, not referenced, so history survives deleting or editing the alarm. */
@Entity(tableName = "emergency_explanation")
data class EmergencyExplanationEntity(
    @PrimaryKey(autoGenerate = true)
    val explanationId: Long = 0,
    val alarmId: Long?,
    val alarmLabel: String?,
    val alarmHourOfDay: Int?,
    val alarmMinute: Int?,
    val dismissalSummary: String?,
    val createdAtMillis: Long,
    val text: String
)
