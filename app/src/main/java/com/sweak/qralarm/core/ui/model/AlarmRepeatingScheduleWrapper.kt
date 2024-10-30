package com.sweak.qralarm.core.ui.model

import java.time.DayOfWeek

data class AlarmRepeatingScheduleWrapper(
    val alarmRepeatingMode: AlarmRepeatingMode = AlarmRepeatingMode.ONLY_ONCE,
    val alarmDaysOfWeek: List<DayOfWeek> = emptyList()
) {
    enum class AlarmRepeatingMode {
        ONLY_ONCE, MON_FRI, SAT_SUN, EVERYDAY, CUSTOM
    }
}