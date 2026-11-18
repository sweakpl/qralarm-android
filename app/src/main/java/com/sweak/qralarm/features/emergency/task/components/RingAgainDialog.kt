package com.sweak.qralarm.features.emergency.task.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.RingAgainLater
import java.time.LocalTime

/** After an emergency dismissal: optionally make the alarm ring once more later. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingAgainDialog(
    onRingInMinutes: (Int) -> Unit,
    onRingAt: (hourOfDay: Int, minute: Int) -> Unit,
    onDecline: () -> Unit
) {
    var pickTime by rememberSaveable { mutableStateOf(false) }
    if (pickTime) {
        val now = LocalTime.now().plusHours(1)
        val timeState = rememberTimePickerState(initialHour = now.hour, initialMinute = now.minute)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            title = { Text(stringResource(R.string.ring_again_pick_time)) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = { onRingAt(timeState.hour, timeState.minute) }) {
                    Text(stringResource(R.string.ring_again_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { pickTime = false }) { Text(stringResource(R.string.cancel)) } }
        )
        return
    }
    AlertDialog(
        onDismissRequest = { /* An explicit choice is required. */ },
        title = { Text(stringResource(R.string.ring_again_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.ring_again_description))
                RingAgainLater.QUICK_DELAYS_IN_MINUTES.forEach { minutes ->
                    OutlinedButton(onClick = { onRingInMinutes(minutes) }) {
                        Text(stringResource(R.string.ring_again_in_minutes, minutes))
                    }
                }
                OutlinedButton(onClick = { pickTime = true }) { Text(stringResource(R.string.ring_again_pick_time)) }
            }
        },
        confirmButton = { TextButton(onClick = onDecline) { Text(stringResource(R.string.ring_again_no)) } }
    )
}
