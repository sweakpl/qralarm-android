package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import com.sweak.qralarm.R
import com.sweak.qralarm.databinding.QralarmTimePickerBinding

class QRAlarmTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.timePickerStyle
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var timePicker: TimePicker

    init {
        initQRAlarmTimePicker()
    }

    private fun initQRAlarmTimePicker() {
        timePicker = QralarmTimePickerBinding.bind(
            inflate(context, R.layout.qralarm_time_picker, this)
        ).timePicker

        timePicker.descendantFocusability = FOCUS_BLOCK_DESCENDANTS
    }

    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener) {
        timePicker.setOnTimeChangedListener(onTimeChangedListener)
    }

    fun setIs24HourView(is24HourView: Boolean) {
        timePicker.setIs24HourView(is24HourView)
    }

    @Suppress("DEPRECATION")
    fun setTime(hour: Int, minute: Int) = timePicker.apply {
        this.hour = hour
        this.minute = minute
    }
}