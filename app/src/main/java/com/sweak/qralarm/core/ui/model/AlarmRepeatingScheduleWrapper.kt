package com.sweak.qralarm.core.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek

@Parcelize
data class AlarmRepeatingScheduleWrapper(
    val alarmRepeatingMode: AlarmRepeatingMode = AlarmRepeatingMode.ONLY_ONCE,
    val alarmDaysOfWeek: List<DayOfWeek> = emptyList()
) : Parcelable {
    enum class AlarmRepeatingMode {
        ONLY_ONCE, MON_FRI, SAT_SUN, EVERYDAY, CUSTOM
    }
}