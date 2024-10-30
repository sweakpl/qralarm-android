package com.sweak.qralarm.core.ui.compose_util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode
import com.sweak.qralarm.core.ui.shortName
import java.time.DayOfWeek

@Composable
fun getAlarmRepeatingScheduleString(
    alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper
): String {
    return when (alarmRepeatingScheduleWrapper.alarmRepeatingMode) {
        AlarmRepeatingMode.ONLY_ONCE -> stringResource(R.string.only_once)
        AlarmRepeatingMode.MON_FRI -> {
            DayOfWeek.MONDAY.shortName() + " - " + DayOfWeek.FRIDAY.shortName()
        }
        AlarmRepeatingMode.SAT_SUN -> {
            DayOfWeek.SATURDAY.shortName() + ", " + DayOfWeek.SUNDAY.shortName()
        }
        AlarmRepeatingMode.EVERYDAY -> stringResource(R.string.everyday)
        AlarmRepeatingMode.CUSTOM -> {
            val days = alarmRepeatingScheduleWrapper.alarmDaysOfWeek

            if (days.size == 1) {
                return days.first().shortName()
            } else if (days.size == 2) {
                return days.joinToString { it.shortName() }
            } else if (areAllDaysAfterOneAnother(days)) {
                return days.first().shortName() + " - " + days.last().shortName()
            } else {
                return days.joinToString { it.shortName() }
            }
        }
    }
}

private fun areAllDaysAfterOneAnother(days: List<DayOfWeek>): Boolean {
    days.forEachIndexed { index, day ->
        if (index == days.size - 1) return true
        if (days[index + 1].value - day.value != 1) return false
    }

    return false
}