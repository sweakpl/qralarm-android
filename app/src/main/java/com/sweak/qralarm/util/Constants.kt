package com.sweak.qralarm.util

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.sweak.qralarm.R

enum class TimeFormat {
    MILITARY, AMPM;

    companion object {
        fun fromInt(ordinal: Int) = entries.firstOrNull { it.ordinal == ordinal }
    }
}

enum class AlarmSound(@RawRes val resourceId: Int, @StringRes val nameResourceId: Int) {
    GENTLE_GUITAR(R.raw.gentle_guitar, R.string.gentle_guitar),
    ALARM_CLOCK(R.raw.alarm_clock, R.string.alarm_clock),
    AIR_HORN(R.raw.air_horn, R.string.air_horn),
    LOCAL_SOUND(-1, R.string.local_sound);

    companion object {
        fun fromInt(ordinal: Int) = entries.firstOrNull { it.ordinal == ordinal }
    }
}

enum class SnoozeDuration(val lengthMinutes: Int) {
    SNOOZE_DURATION_20_MINUTES(20),
    SNOOZE_DURATION_15_MINUTES(15),
    SNOOZE_DURATION_10_MINUTES(10),
    SNOOZE_DURATION_5_MINUTES(5),
    SNOOZE_DURATION_3_MINUTES(3),
    SNOOZE_DURATION_2_MINUTES(2);

    override fun toString(): String {
        return lengthMinutes.toString()
    }

    companion object {
        fun fromInt(lengthMinutes: Int) = entries.firstOrNull { it.lengthMinutes == lengthMinutes }
    }
}

enum class SnoozeMaxCount(val count: Int) {
    SNOOZE_MAX_COUNT_3(3),
    SNOOZE_MAX_COUNT_2(2),
    SNOOZE_MAX_COUNT_1(1),
    SNOOZE_MAX_COUNT_0(0);

    override fun toString(): String {
        return count.toString()
    }

    companion object {
        fun fromInt(count: Int) = entries.firstOrNull { it.count == count }
    }
}

enum class GentleWakeupDuration(val lengthSeconds: Int) {
    GENTLE_WAKEUP_DURATION_60_SECONDS(60),
    GENTLE_WAKEUP_DURATION_30_SECONDS(30),
    GENTLE_WAKEUP_DURATION_10_SECONDS(10),
    GENTLE_WAKEUP_DURATION_0_SECONDS(0);

    fun inMillis(): Long = (lengthSeconds * 1000).toLong()

    override fun toString(): String {
        return lengthSeconds.toString()
    }

    companion object {
        fun fromInt(lengthSeconds: Int) = entries.firstOrNull { it.lengthSeconds == lengthSeconds }
    }
}

const val KEY_SCANNER_MODE = "scannerMode"
const val SCAN_MODE_DISMISS_ALARM = "scanModeDismissAlarm"
const val SCAN_MODE_SET_CUSTOM_CODE = "scanModeSetCustomCode"

const val LOCK_SCREEN_VISIBILITY_FLAG = "lockScreenVisibilityFlag"

const val DEFAULT_DISMISS_ALARM_CODE = "StopAlarm"

const val ALARM_PENDING_INTENT_REQUEST_CODE = 100
const val ALARM_INFO_PENDING_INTENT_REQUEST_CODE = 101

const val ALARM_SET_INDICATION_NOTIFICATION_ID = 103
const val ALARM_SET_INDICATION_NOTIFICATION_REQUEST_CODE = 104
const val CANCEL_ALARM_ACTION_REQUEST_CODE = 105
const val ALARM_MISSED_NOTIFICATION_ID = 106
const val ALARM_MISSED_NOTIFICATION_REQUEST_CODE = 107
const val POST_UPCOMING_ALARM_NOTIFICATION_ACTION_REQUEST_CODE = 108

const val HANDLER_THREAD_NAME = "QRAlarmHandlerThread"
const val LOCAL_HANDLER_MESSAGE_IDENTIFIER = 299
const val FOREGROUND_SERVICE_ID = 300
const val ALARM_NOTIFICATION_ID = 300
const val ALARM_NOTIFICATION_REQUEST_CODE = 400
const val ALARM_FULL_SCREEN_REQUEST_CODE = 500
const val ACTION_TEMPORARY_ALARM_SOUND_MUTE = "com.sweak.qralarm.TEMPORARY_ALARM_SOUND_MUTE"

const val KEY_ALARM_TYPE = "alarmType"
const val ALARM_TYPE_NORMAL = 200
const val ALARM_TYPE_SNOOZE = 201

const val ALARM_NOTIFICATION_CHANNEL_ID = "QRAlarmNotificationChannelId"
const val ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID = "QRAlarmSetIndicationNotificationChannelId"