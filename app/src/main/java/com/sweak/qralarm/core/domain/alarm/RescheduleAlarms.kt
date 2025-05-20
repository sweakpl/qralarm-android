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
            val fiveMinutesInMillis = 5 * 60000
            val currentTimeInMillis = System.currentTimeMillis()

            alarmsRepository.getAllAlarms().first().forEach { alarm ->
                if (alarm.snoozeConfig.isAlarmSnoozed &&
                    alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis != null
                ) {
                    val snoozedAlarmTimeInMillis = alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis

                    // The snoozed alarm has been missed:
                    if (snoozedAlarmTimeInMillis < currentTimeInMillis) {
                        // If it was missed by less than five minutes - reschedule:
                        if (snoozedAlarmTimeInMillis + fiveMinutesInMillis > currentTimeInMillis) {
                            snoozeAlarm(
                                alarmId = alarm.alarmId,
                                isReschedulingCurrentOrMissedSnooze = true
                            )
                        // If it was missed by more than five minutes - notify the user:
                        } else {
                            qrAlarmManager.notifyAboutMissedAlarm()
                            userDataRepository.setAlarmMissedDetected(detected = true)

                            if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                                disableAlarm(alarmId = alarm.alarmId)
                            } else {
                                alarmsRepository.setAlarmSnoozed(
                                    alarmId = alarm.alarmId,
                                    snoozed = false
                                )
                                setAlarm(
                                    alarmId = alarm.alarmId,
                                    isReschedulingMissedAlarm = false
                                )
                            }
                        }
                    // The snoozed alarm is still in the future - reschedule:
                    } else {
                        snoozeAlarm(
                            alarmId = alarm.alarmId,
                            isReschedulingCurrentOrMissedSnooze = true
                        )
                    }
                } else if (alarm.isAlarmEnabled) {
                    val alarmTimeInMillis = alarm.nextAlarmTimeInMillis

                    // The alarm has been missed
                    if (alarmTimeInMillis < currentTimeInMillis) {
                        // If it was missed by less than five minutes - reschedule:
                        if (alarmTimeInMillis + fiveMinutesInMillis > currentTimeInMillis) {
                            setAlarm(
                                alarmId = alarm.alarmId,
                                isReschedulingMissedAlarm = true
                            )
                        // If it was missed by more than five minutes - notify the user:
                        } else {
                            qrAlarmManager.notifyAboutMissedAlarm()
                            userDataRepository.setAlarmMissedDetected(detected = true)

                            if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                                disableAlarm(alarmId = alarm.alarmId)
                            } else {
                                setAlarm(
                                    alarmId = alarm.alarmId,
                                    isReschedulingMissedAlarm = false
                                )
                            }
                        }
                    // The alarm is still in the future - reschedule:
                    } else {
                        setAlarm(
                            alarmId = alarm.alarmId,
                            isReschedulingMissedAlarm = false
                        )
                    }
                }
            }
        }
    }
}