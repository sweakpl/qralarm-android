package com.sweak.qralarm.features.add_edit_alarm.components

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateMargins
import com.sweak.qralarm.R
import com.sweak.qralarm.databinding.QralarmTimePickerBinding
import kotlin.math.roundToInt

class QRAlarmTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.timePickerStyle
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: QralarmTimePickerBinding

    private var marginAnimator: ValueAnimator? = null

    private var isFirstTimeUpdatingEnabledState = true

    companion object {
        private const val MARGIN_ANIMATION_DURATION_MS = 250L
    }

    init {
        initQRAlarmTimePicker()
    }

    private fun initQRAlarmTimePicker() {
        binding = QralarmTimePickerBinding.bind(
            inflate(context, R.layout.qralarm_time_picker, this)
        )

        binding.timePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS
    }

    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener) {
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener)
    }

    fun setIs24HourView(is24HourView: Boolean) {
        binding.timePicker.setIs24HourView(is24HourView)
    }

    fun setTime(hour: Int, minute: Int) = binding.timePicker.apply {
        this.hour = hour
        this.minute = minute
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        binding.timePicker.apply {
            if (isFirstTimeUpdatingEnabledState) {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT, dpToPx(212f).roundToInt()
                ).apply {
                    updateMargins(
                        top = if (enabled) 0 else dpToPx(-74f).roundToInt(),
                        bottom = if (enabled) 0 else dpToPx(-74f).roundToInt()
                    )
                }

                isFirstTimeUpdatingEnabledState = false
                isEnabled = enabled

                return
            }

            marginAnimator?.end()

            if (isEnabled == enabled) return

            isEnabled = enabled

            marginAnimator = if (enabled) {
                if (marginTop != 0 || marginBottom != 0)
                    ValueAnimator.ofFloat(-74f, 0f)
                else return
            } else {
                if (marginTop != -74 || marginBottom != -74)
                    ValueAnimator.ofFloat(0f, -74f)
                else return
            }

            marginAnimator?.apply {
                duration = MARGIN_ANIMATION_DURATION_MS
                interpolator = DecelerateInterpolator()

                addUpdateListener {
                    layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT, dpToPx(212f).roundToInt()
                    ).apply {
                        updateMargins(
                            top = dpToPx(it.animatedValue as Float).roundToInt(),
                            bottom = dpToPx(it.animatedValue as Float).roundToInt()
                        )
                    }
                }

                start()
            }
        }
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}