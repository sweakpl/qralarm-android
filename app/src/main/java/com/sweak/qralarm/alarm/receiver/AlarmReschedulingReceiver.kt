package com.sweak.qralarm.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReschedulingReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var rescheduleAlarms: RescheduleAlarms

    private val intentActionsToFilter = listOf(
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.LOCKED_BOOT_COMPLETED",
        "android.intent.action.ACTION_BOOT_COMPLETED",
        "android.intent.action.REBOOT",
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
        "android.intent.action.MY_PACKAGE_REPLACED",
        "android.intent.action.TIME_SET",
        "android.intent.action.DATE_CHANGED",
        "android.intent.action.TIMEZONE_CHANGED"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) receiverScope.launch {
            if (intent.action == "android.intent.action.MY_PACKAGE_REPLACED") {
                try {
                    if (!userDataRepository.isLegacyDataMigrated.first()) {
                        userDataRepository.setLegacyDataMigrated(migrated = true)
                        migrateLegacyData(context)
                    }
                } catch (ignored: IOException) { /* no-op */ }
            }

            rescheduleAlarms()
        }
    }

    private suspend fun migrateLegacyData(context: Context) {
        val preferences = context.dataStore.data.firstOrNull()?.asMap()

        if (preferences.isNullOrEmpty()) return

        val alarmTimeInMillis = preferences[longPreferencesKey("alarmTimeInMillis")] as Long
        val alarmSet = preferences[booleanPreferencesKey("alarmSet")] as Boolean
        val snoozeAlarmTimeInMillis =
            preferences[longPreferencesKey("snoozeAlarmTimeInMillis")] as? Long
        val alarmSnoozed = preferences[booleanPreferencesKey("alarmSnoozed")] as Boolean
        val manualAlarmScheduling =
            preferences[booleanPreferencesKey("manualAlarmScheduling")] as? Boolean
        val snoozeMaxCount = preferences[intPreferencesKey("snoozeMaxCount")] as Int
        val snoozeDurationMinutes =
            preferences[intPreferencesKey("snoozeDurationMinutes")] as Int
        val snoozeAvailableCount =
            preferences[intPreferencesKey("snoozeAvailableCount")] as? Int
        val alarmSound = preferences[intPreferencesKey("alarmSound")] as Int
        val enableVibrations =
            preferences[booleanPreferencesKey("enableVibrations")] as? Boolean
        val dismissAlarmCode = preferences[stringPreferencesKey("dismissAlarmCode")] as String
        val gentleWakeUpDurationSeconds =
            preferences[intPreferencesKey("gentleWakeupDurationSeconds")] as? Int
        val temporaryAlarmMuteDisabled =
            preferences[booleanPreferencesKey("temporaryAlarmMuteDisabled")] as? Boolean

        val alarmId = 1L
        val alarmDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(alarmTimeInMillis),
            ZoneId.systemDefault()
        )
        val ringtone = when (alarmSound) {
            0 -> Alarm.Ringtone.GENTLE_GUITAR
            1 -> Alarm.Ringtone.ALARM_CLOCK
            2 -> Alarm.Ringtone.AIR_HORN
            3 -> Alarm.Ringtone.CUSTOM_SOUND
            else -> Alarm.Ringtone.GENTLE_GUITAR
        }
        val customRingtoneUriString = if (ringtone == Alarm.Ringtone.CUSTOM_SOUND) {
            File(context.filesDir, "qralarm_user_selected_alarm_sound").run {
                if (exists()) {
                    val newFile = File(context.filesDir, alarmId.toString())

                    if (renameTo(newFile)) {
                        Uri.fromFile(newFile).toString()
                    } else null
                } else null
            }
        } else null

        val alarmToSave = Alarm(
            alarmId = alarmId,
            alarmHourOfDay = alarmDateTime.hour,
            alarmMinute = alarmDateTime.minute,
            isAlarmEnabled = alarmSet,
            isAlarmRunning = false,
            repeatingMode =
            if (manualAlarmScheduling == true) Alarm.RepeatingMode.Once
            else Alarm.RepeatingMode.Days(DayOfWeek.entries),
            nextAlarmTimeInMillis = alarmTimeInMillis,
            snoozeConfig = Alarm.SnoozeConfig(
                snoozeMode = Alarm.SnoozeMode(
                    numberOfSnoozes = snoozeMaxCount,
                    snoozeDurationInMinutes = snoozeDurationMinutes
                ),
                numberOfSnoozesLeft = snoozeAvailableCount ?: snoozeMaxCount,
                isAlarmSnoozed = alarmSnoozed,
                nextSnoozedAlarmTimeInMillis = snoozeAlarmTimeInMillis
            ),
            ringtone = ringtone,
            customRingtoneUriString = customRingtoneUriString,
            areVibrationsEnabled = enableVibrations.run { this ?: true },
            isUsingCode = true,
            assignedCode = dismissAlarmCode,
            isOpenCodeLinkEnabled = false,
            isOneHourLockEnabled = true,
            alarmLabel = null,
            gentleWakeUpDurationInSeconds = gentleWakeUpDurationSeconds ?: 0,
            temporaryMuteDurationInSeconds = temporaryAlarmMuteDisabled.run {
                if (this != null) {
                    if (this) 0 else 15
                }
                else 0
            },
            skipAlarmUntilTimeInMillis = null
        )

        alarmsRepository.addOrEditAlarm(alarm = alarmToSave)
    }
}