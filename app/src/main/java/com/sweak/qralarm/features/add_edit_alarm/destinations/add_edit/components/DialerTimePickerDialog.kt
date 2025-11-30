package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerTimePickerDialog(
    initialHourOfDay: Int,
    initialMinute: Int,
    onDismissRequest: () -> Unit,
    onTimeConfirmed: (hourOfDay: Int, minute: Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
            Column(modifier = Modifier.wrapContentSize()) {
                val timePickerState = rememberTimePickerState(
                    initialHour = initialHourOfDay,
                    initialMinute = initialMinute,
                    is24Hour = DateFormat.is24HourFormat(LocalContext.current)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.onSurface,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    layoutType = TimePickerLayoutType.Vertical,
                    modifier = Modifier
                        .padding(top = MaterialTheme.space.medium)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = MaterialTheme.space.medium)
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                    Button(
                        onClick = {
                            onTimeConfirmed(timePickerState.hour, timePickerState.minute)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.ok),
                            modifier = Modifier.padding(MaterialTheme.space.xSmall)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialerTimePickerDialogPreview() {
    QRAlarmTheme {
        DialerTimePickerDialog(
            initialHourOfDay = 8,
            initialMinute = 30,
            onDismissRequest = {},
            onTimeConfirmed = { _, _ -> }
        )
    }
}