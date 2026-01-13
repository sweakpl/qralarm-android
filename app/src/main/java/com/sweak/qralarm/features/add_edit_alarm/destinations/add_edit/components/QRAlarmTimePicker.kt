package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import android.text.format.DateFormat
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.LocalUseDynamicTheming

@Composable
fun QRAlarmTimePicker(
    selectedHourOfDay: Int,
    selectedMinute: Int,
    onTimeChanged: (hourOfDay: Int, minute: Int) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val useDynamicTheming = LocalUseDynamicTheming.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val themeRes = if (useDynamicTheming) {
                R.style.TimePickerStyle
            } else {
                R.style.TimePickerStyleStatic
            }
            val themedContext = ContextThemeWrapper(context, themeRes)
            QRAlarmTimePicker(themedContext).apply {
                // Right after composing the TimePicker the internal TimePicker(View) calls the
                // timeChangedListener with the current time which breaks the uiState - we have to
                // prevent the uiState update after this initial timeChangedListener call:
                var isInitialUpdate = true
                setIs24HourView(DateFormat.is24HourFormat(context))

                setOnTimeChangedListener { _, hourOfDay, minute ->
                    if (!isInitialUpdate) {
                        onTimeChanged(hourOfDay, minute)
                    } else {
                        isInitialUpdate = false
                    }
                }
            }
        },
        update = {
            it.setTime(selectedHourOfDay, selectedMinute)
            it.isEnabled = isEnabled
        }
    )
}