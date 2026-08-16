package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class CopyAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val alarmRingtoneStorage: AlarmRingtoneStorage
) {
    suspend operator fun invoke(alarmId: Long) {
        val sourceAlarm = alarmsRepository.getAlarm(alarmId = alarmId) ?: return

        val copiedAlarm = sourceAlarm.copy(
            alarmId = 0,
            isAlarmEnabled = false,
            isAlarmRunning = false,
            skipAlarmUntilTimeInMillis = null
        )

        val newAlarmId = alarmsRepository.addOrEditAlarm(alarm = copiedAlarm)

        if (sourceAlarm.customRingtoneUriString == null) return

        val duplicatedRingtoneUri = alarmRingtoneStorage.duplicateForAlarm(
            sourceAlarmId = alarmId,
            newAlarmId = newAlarmId
        )

        if (duplicatedRingtoneUri != null) {
            alarmsRepository.setAlarmRingtoneUri(alarmId = newAlarmId, uri = duplicatedRingtoneUri)
        } else {
            // Fall back to default alarm if copy failed:
            alarmsRepository.setAlarmRingtoneUri(alarmId = newAlarmId, uri = null)
            alarmsRepository.setAlarmRingtone(
                alarmId = newAlarmId,
                ringtone = Alarm.Ringtone.GENTLE_GUITAR
            )
        }
    }
}
