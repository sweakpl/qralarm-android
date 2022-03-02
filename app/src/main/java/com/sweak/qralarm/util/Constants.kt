package com.sweak.qralarm.util

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.sweak.qralarm.R

enum class TimeFormat {
    MILITARY, AMPM;

    companion object {
        fun fromInt(ordinal: Int) = values().firstOrNull { it.ordinal == ordinal }
    }
}

enum class Meridiem {
    AM, PM
}

enum class AlarmSound(@RawRes val resourceId: Int, @StringRes val nameResourceId: Int) {
    DEFAULT_SYSTEM(0, R.string.default_system),
    GENTLE_GUITAR(R.raw.gentle_guitar, R.string.gentle_guitar),
    ALARM_CLOCK(R.raw.alarm_clock, R.string.alarm_clock),
    AIR_HORN(R.raw.air_horn, R.string.air_horn);

    companion object {
        fun fromInt(ordinal: Int) = values().firstOrNull { it.ordinal == ordinal }
    }
}

val AVAILABLE_ALARM_SOUNDS = listOf(
    AlarmSound.DEFAULT_SYSTEM,
    AlarmSound.GENTLE_GUITAR,
    AlarmSound.ALARM_CLOCK,
    AlarmSound.AIR_HORN,
)

const val SNOOZE_DURATION_10_MINUTES = 10
const val SNOOZE_DURATION_5_MINUTES = 5
const val SNOOZE_DURATION_3_MINUTES = 3
const val SNOOZE_DURATION_2_MINUTES = 2

val AVAILABLE_SNOOZE_DURATIONS = listOf(
    SNOOZE_DURATION_10_MINUTES,
    SNOOZE_DURATION_5_MINUTES,
    SNOOZE_DURATION_3_MINUTES,
    SNOOZE_DURATION_2_MINUTES
)

const val SNOOZE_MAX_COUNT_3 = 3
const val SNOOZE_MAX_COUNT_2 = 2
const val SNOOZE_MAX_COUNT_1 = 1
const val SNOOZE_MAX_COUNT_0 = 0

val AVAILABLE_SNOOZE_MAX_COUNTS = listOf(
    SNOOZE_MAX_COUNT_3,
    SNOOZE_MAX_COUNT_2,
    SNOOZE_MAX_COUNT_1,
    SNOOZE_MAX_COUNT_0
)

const val KEY_SCANNER_MODE = "scannerMode"
const val SCAN_MODE_DISMISS_ALARM = "scanModeDismissAlarm"
const val SCAN_MODE_SET_CUSTOM_CODE = "scanModeSetCustomCode"