package com.sweak.qralarm.core.domain.alarm

import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.user.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RescheduleAlarms @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val userDataRepository: UserDataRepository,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm,
    private val snoozeAlarm: SnoozeAlarm
) {
    suspend operator fun invoke() {
        if (!qrAlarmManager.canScheduleExactAlarms()) {
            alarmsRepository.getAllAlarms().first().forEach { alarm ->
                disableAlarm(alarmId = alarm.alarmId)
            }
        } else {
            alarmsRepository.getAllAlarms().first().forEach { alarm ->
                if (alarm.snoozeConfig.isAlarmSnoozed &&
                    alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis != null
                ) {
                    if (alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis < System.currentTimeMillis()) {
                        qrAlarmManager.notifyAboutMissedAlarm()
                        userDataRepository.setAlarmMissedDetected(detected = true)

                        if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                            disableAlarm(alarmId = alarm.alarmId)
                        } else {
                            alarmsRepository.setAlarmSnoozed(
                                alarmId = alarm.alarmId,
                                snoozed = false
                            )
                            setAlarm(alarmId = alarm.alarmId)
                        }
                    } else {
                        snoozeAlarm(
                            alarmId = alarm.alarmId,
                            isReschedulingCurrentSnooze = true
                        )
                    }
                } else if (alarm.isAlarmEnabled) {
                    if (alarm.nextAlarmTimeInMillis < System.currentTimeMillis()) {
                        qrAlarmManager.notifyAboutMissedAlarm()
                        userDataRepository.setAlarmMissedDetected(detected = true)

                        if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                            disableAlarm(alarmId = alarm.alarmId)
                        } else {
                            setAlarm(alarmId = alarm.alarmId)
                        }
                    } else {
                        setAlarm(alarmId = alarm.alarmId)
                    }
                }
            }
        }
    }
}