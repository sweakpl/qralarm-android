package com.sweak.qralarm.util

enum class TimeFormat {
    MILITARY, AMPM;

    companion object {
        fun fromInt(ordinal: Int) = values().firstOrNull { it.ordinal == ordinal }
    }
}

enum class Meridiem {
    AM, PM
}

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