package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.TextView
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timePicker.importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
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

    fun setTextColor(color: Int) {
        setTextColorRecursive(timePicker, color)
    }

    private fun setTextColorRecursive(view: View, color: Int) {
        when (view) {
            is NumberPicker -> setNumberPickerTextColor(view, color)
            is TextView -> view.setTextColor(color)
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    setTextColorRecursive(view.getChildAt(i), color)
                }
            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        // For API 29+, use the public API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPicker.textColor = color
        } else {
            // For older APIs, use reflection to set mSelectorWheelPaint
            try {
                val wheelPaintField =
                    NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                wheelPaintField.isAccessible = true
                val paint = wheelPaintField.get(numberPicker) as? Paint
                paint?.color = color
                numberPicker.invalidate()
            } catch (_: Exception) { /* no-op */ }

            // Also set EditText (center) color separately
            for (i in 0 until numberPicker.childCount) {
                val child = numberPicker.getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(color)
                }
            }
        }
    }
}