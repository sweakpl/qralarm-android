package com.sweak.qralarm.util

fun swapTimeFormats(
    timeFormatString: String,
    is24HourFormat: Boolean,
    hour: Int,
    meridiemString: String? = null
): Triple<Int?, String?, String?> {
    if (is24HourFormat && timeFormatString == TimeFormat.AMPM.name && meridiemString != null) {
        return parseAmPmHourToMilitary(hour, meridiemString)
    } else if (!is24HourFormat && timeFormatString == TimeFormat.MILITARY.name) {
        return parseMilitaryHourToAmPm(hour)
    }

    return Triple(null, null, null)
}

private fun parseAmPmHourToMilitary(
    hour: Int,
    meridiemString: String
): Triple<Int, String, String?> {
    var newHour = hour

    if (hour == 12) {
        newHour = 0
    }
    if (meridiemString == Meridiem.PM.name) {
        newHour += 12
    }

    return Triple(newHour, TimeFormat.MILITARY.name, null)
}

private fun parseMilitaryHourToAmPm(hour: Int): Triple<Int, String, String> {
    var newHour = hour
    val newMeridiemString: String

    when {
        hour > 12 -> {
            newHour -= 12
            newMeridiemString = Meridiem.PM.name
        }
        hour == 12 -> {
            newMeridiemString = Meridiem.PM.name
        }
        else -> {
            newMeridiemString = Meridiem.AM.name
        }
    }

    return Triple(newHour, TimeFormat.AMPM.name, newMeridiemString)
}