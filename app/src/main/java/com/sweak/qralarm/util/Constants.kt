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
    GENTLE_GUITAR(R.raw.gentle_guitar, R.string.gentle_guitar),
    ALARM_CLOCK(R.raw.alarm_clock, R.string.alarm_clock),
    AIR_HORN(R.raw.air_horn, R.string.air_horn),
    USER_FILE(R.raw.gentle_guitar, R.string.user_alarm_file);

    companion object {
        fun fromInt(ordinal: Int) = values().firstOrNull { it.ordinal == ordinal }
    }
}

val AVAILABLE_ALARM_SOUNDS = listOf(
    AlarmSound.GENTLE_GUITAR,
    AlarmSound.ALARM_CLOCK,
    AlarmSound.AIR_HORN,
    AlarmSound.USER_FILE,
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

const val LOCK_SCREEN_VISIBILITY_FLAG = "lockScreenVisibilityFlag"

const val DEFAULT_DISMISS_ALARM_CODE = "StopAlarm"

const val ALARM_PENDING_INTENT_REQUEST_CODE = 100
const val ALARM_INFO_PENDING_INTENT_REQUEST_CODE = 101
const val TESTING_PERMISSION_ALARM_INTENT_REQUEST_CODE = 102

const val HANDLER_THREAD_NAME = "QRAlarmHandlerThread"
const val FOREGROUND_SERVICE_ID = 300
const val ALARM_NOTIFICATION_ID = 300
const val ALARM_NOTIFICATION_REQUEST_CODE = 400
const val ALARM_FULL_SCREEN_REQUEST_CODE = 500

const val KEY_ALARM_TYPE = "alarmType"
const val ALARM_TYPE_NORMAL = 200
const val ALARM_TYPE_SNOOZE = 201
const val ALARM_TYPE_NONE = 202

const val ALARM_NOTIFICATION_CHANNEL_ID = "QRAlarmNotificationChannelId"