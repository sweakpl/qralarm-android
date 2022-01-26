package com.sweak.qralarm.ui.screens.home

import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.TimeFormat

data class HomeUiState(
    var timeFormat: TimeFormat,
    var hour: Int,
    var minute: Int,
    var meridiem: Meridiem,
    var alarmSet: Boolean
)