package com.sweak.qralarm.core.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Long = 0,
    val alarmHourOfDay: Int,
    val alarmMinute: Int,
    val isAlarmEnabled: Boolean,
    val isAlarmRunning: Boolean,
    val nextAlarmTimeInMillis: Long,
    val repeatingAlarmDays: String?,
    val numberOfSnoozes: Int,
    val snoozeDurationInMinutes: Int,
    val numberOfSnoozesLeft: Int,
    val isAlarmSnoozed: Boolean,
    val nextSnoozedAlarmTimeInMillis: Long?,
    val ringtone: String,
    val customRingtoneUriString: String?,
    val areVibrationsEnabled: Boolean,
    val isUsingCode: Boolean,
    val assignedCode: String?,
    @ColumnInfo(defaultValue = "FALSE") val isOpenCodeLinkEnabled: Boolean,
    @ColumnInfo(defaultValue = "TRUE") val isOneHourLockEnabled: Boolean,
    val alarmLabel: String?,
    val gentleWakeUpDurationInSeconds: Int,
    val temporaryMuteDurationInSeconds: Int,
    val skipAlarmUntilTimeInMillis: Long?
)
