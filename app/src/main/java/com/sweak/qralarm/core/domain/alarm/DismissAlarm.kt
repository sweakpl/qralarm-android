package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.core.domain.user.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DismissAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val disableAlarm: DisableAlarm,
    private val advanceAlarmToNextOccurrence: AdvanceAlarmToNextOccurrence,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(alarmId: Long, reason: Reason): Result {
        val alarm = alarmsRepository.getAlarm(alarmId = alarmId)

        if (alarm == null) {
            disableAlarm(alarmId = alarmId)
            return Result.Finished
        }

        return when (reason) {
            Reason.StoppedWithoutCode, Reason.CodeScanned -> dismissRingingAlarm(alarm)
            Reason.CodeScannedBeforeAlarmFired -> dismissBeforeAlarmFired(alarm)
            Reason.EmergencyTaskCompleted -> dismissWithEmergencyTask(alarm)
        }
    }

    private suspend fun dismissRingingAlarm(alarm: Alarm): Result {
        alarmsRepository.setAlarmSnoozed(alarmId = alarm.alarmId, snoozed = false)
        advanceAlarmToNextOccurrence(alarmId = alarm.alarmId)
        return Result.Finished
    }

    private suspend fun dismissBeforeAlarmFired(alarm: Alarm): Result {
        alarmsRepository.setAlarmSnoozed(alarmId = alarm.alarmId, snoozed = false)
        disableAlarm(alarmId = alarm.alarmId)
        alarmsRepository.setSkipNextAlarm(alarmId = alarm.alarmId, skip = true)
        advanceAlarmToNextOccurrence(alarmId = alarm.alarmId)
        return Result.Finished
    }

    private suspend fun dismissWithEmergencyTask(alarm: Alarm): Result {
        val isAlarmRepeating = alarm.repeatingMode is Alarm.RepeatingMode.Days
        val shouldDisableRepeatingAlarms =
            userDataRepository.isEmergencyDisableRepeatingAlarmsEnabled.first()

        if (!isAlarmRepeating || shouldDisableRepeatingAlarms) {
            disableAlarm(alarmId = alarm.alarmId)
        } else if (alarm.snoozeConfig.isAlarmSnoozed) {
            alarmsRepository.setAlarmSnoozed(alarmId = alarm.alarmId, snoozed = false)
            advanceAlarmToNextOccurrence(alarmId = alarm.alarmId)
        }

        return if (isAlarmRepeating && shouldDisableRepeatingAlarms) {
            Result.RepeatingAlarmDisabled
        } else {
            Result.Finished
        }
    }

    enum class Reason {
        StoppedWithoutCode,
        CodeScanned,
        CodeScannedBeforeAlarmFired,
        EmergencyTaskCompleted
    }

    sealed class Result {
        data object Finished : Result()
        data object RepeatingAlarmDisabled : Result()
    }
}
