package com.sweak.qralarm.alarm.service

import com.sweak.qralarm.core.domain.alarm.Alarm
import java.time.Clock
import javax.inject.Inject

class AlarmDeliveryValidator @Inject constructor(
    private val clock: Clock
) {
    fun validate(alarm: Alarm?, isSnoozeAlarm: Boolean?): Verdict {
        if (alarm == null) return Verdict.StopEarly

        val scheduledTimeInMillis = when (isSnoozeAlarm) {
            true -> alarm.snoozeConfig.nextSnoozedAlarmTimeInMillis
            else -> alarm.nextAlarmTimeInMillis
        } ?: return Verdict.Ring(waitMillis = 0)
        val launchDelayInMillis = clock.millis() - scheduledTimeInMillis

        if (launchDelayInMillis !in -EARLY_LAUNCH_TOLERANCE_MS..ABNORMAL_LAUNCH_TOLERANCE_MS) {
            return Verdict.StopEarly
        }

        return Verdict.Ring(waitMillis = (-launchDelayInMillis).coerceAtLeast(0))
    }

    sealed interface Verdict {
        data class Ring(val waitMillis: Long) : Verdict
        data object StopEarly : Verdict
    }

    private companion object {
        const val ABNORMAL_LAUNCH_TOLERANCE_MS = 10 * 60 * 1000L
        const val EARLY_LAUNCH_TOLERANCE_MS = 5 * 1000L
    }
}
