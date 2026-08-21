package com.sweak.qralarm.core.domain.alarm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MigrateCustomRingtonesToDeviceProtectedStorage @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val alarmRingtoneStorage: AlarmRingtoneStorage
) {
    suspend operator fun invoke() {
        val alarms = alarmsRepository.getAllAlarms().first()

        alarms.forEach { alarm ->
            val currentRingtoneUri = alarm.customRingtoneUriString ?: return@forEach
            val migratedRingtoneUri = withContext(Dispatchers.IO) {
                alarmRingtoneStorage.migrateToDeviceProtectedStorage(alarmId = alarm.alarmId)
            }

            if (migratedRingtoneUri != null && migratedRingtoneUri != currentRingtoneUri) {
                alarmsRepository.setAlarmRingtoneUri(
                    alarmId = alarm.alarmId,
                    uri = migratedRingtoneUri
                )
            }
        }
    }
}
