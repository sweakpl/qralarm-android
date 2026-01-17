package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import android.text.format.DateFormat
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun QRAlarmTimePicker(
    selectedHourOfDay: Int,
    selectedMinute: Int,
    onTimeChanged: (hourOfDay: Int, minute: Int) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentColor = LocalContentColor.current

    AndroidView(
        modifier = modifier,
        factory = {
            QRAlarmTimePicker(it).apply {
                // Set text color programmatically using LocalContentColor
                setTextColor(contentColor.toArgb())
                
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
        update = { view ->
            view.setTime(selectedHourOfDay, selectedMinute)
            view.isEnabled = isEnabled
            view.setTextColor(contentColor.toArgb())
        }
    )
}