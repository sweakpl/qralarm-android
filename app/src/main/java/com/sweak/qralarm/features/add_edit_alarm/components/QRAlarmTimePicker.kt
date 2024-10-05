package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun QRAlarmTimePicker(
    selectedHourOfDay: Int,
    selectedMinute: Int,
    onTimeChanged: (hourOfDay: Int, minute: Int) -> Unit,
    is24HoursView: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = {
            QRAlarmTimePicker(it).apply {
                // Right after composing the TimePicker the internal TimePicker(View) calls the
                // timeChangedListener with the current time which breaks the uiState - we have to
                // prevent the uiState update after this initial timeChangedListener call:
                var isInitialUpdate = true

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
            it.setIs24HourView(is24HoursView)
            it.setTime(selectedHourOfDay, selectedMinute)
            it.isEnabled = isEnabled
        }
    )
}