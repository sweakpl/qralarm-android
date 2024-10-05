package com.sweak.qralarm.core.ui.util

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

fun DayOfWeek.shortName(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}