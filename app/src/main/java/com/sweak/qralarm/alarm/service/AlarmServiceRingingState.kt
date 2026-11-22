package com.sweak.qralarm.alarm.service

import com.sweak.qralarm.core.domain.alarm.AlarmRingingState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmServiceRingingState @Inject constructor() : AlarmRingingState {

    @Volatile
    override var isAnyAlarmRinging = false
        private set

    fun onRingingStarted() {
        isAnyAlarmRinging = true
    }

    fun onRingingStopped() {
        isAnyAlarmRinging = false
    }
}
