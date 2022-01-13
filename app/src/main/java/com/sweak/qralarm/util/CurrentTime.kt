package com.sweak.qralarm.util

import android.content.Context
import android.text.format.DateFormat
import java.util.*

class CurrentTime(context: Context) {

    private val calendar: Calendar = Calendar.getInstance()

    val timeFormat: TimeFormat = if (DateFormat.is24HourFormat(context)) {
        TimeFormat.MILITARY
    } else {
        TimeFormat.AMPM
    }

    val hour: Int = if (timeFormat == TimeFormat.MILITARY) {
        calendar.get(Calendar.HOUR_OF_DAY)
    } else {
        calendar.get(Calendar.HOUR).let {
            if (it == 0) 12 else it
        }
    }

    val minute: Int = calendar.get(Calendar.MINUTE)

    val meridiem: Meridiem = calendar.get(Calendar.AM_PM).let {
        if (it == Calendar.AM) Meridiem.AM else Meridiem.PM
    }
}