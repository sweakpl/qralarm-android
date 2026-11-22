package com.sweak.qralarm.alarm.service.session

import android.media.AudioManager

fun AudioManager.setAlarmVolume(index: Int) {
    try {
        setStreamVolume(
            AudioManager.STREAM_ALARM,
            index,
            0
        )
    } catch (_: SecurityException) {
        /* no-op */
    }
}