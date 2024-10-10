package com.sweak.qralarm.core.storage.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Long = 0,
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val isAlarmEnabled: Boolean,
    val repeatingAlarmOnceDayInMillis: Long?,
    val repeatingAlarmDays: String?,
    val numberOfSnoozes: Int,
    val snoozeDurationInMinutes: Int,
    val ringtone: String,
    val customRingtoneUriString: String?,
    val areVibrationsEnabled: Boolean,
    val isUsingCode: Boolean,
    val assignedCode: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val isTemporaryMuteEnabled: Boolean
)
