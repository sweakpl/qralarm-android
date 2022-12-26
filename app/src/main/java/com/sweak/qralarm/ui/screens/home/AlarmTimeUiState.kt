package com.sweak.qralarm.ui.screens.home
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.TimeFormat
import com.sweak.qralarm.util.currentTimeInMinutes

data class AlarmTimeUiState(
    var timeFormat: TimeFormat,
    var hour: Int,
    var minute: Int,
    var meridiem: Meridiem,
    var currentMinute: Int
)
{
    constructor(
        base: AlarmTimeUiState,
        hour: Int,
        minute: Int,
        meridiem: Meridiem,
    ) : this(
        base.timeFormat,
        hour,
        minute,
        meridiem,
        currentTimeInMinutes()
    )
}