package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.getEarliestOnlyOnceAlarmDate
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDatePickerDialog(
    initialDateInMillis: Long,
    alarmHourOfDay: Int,
    alarmMinute: Int,
    onDismissRequest: () -> Unit,
    onDateConfirmed: (selectedDateInMillis: Long) -> Unit
) {
    val minLocalDate = getEarliestOnlyOnceAlarmDate(alarmHourOfDay, alarmMinute)

    val initialLocalDate = Instant.ofEpochMilli(initialDateInMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .let { if (it.isBefore(minLocalDate)) minLocalDate else it }

    val initialUtcMillis = initialLocalDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val candidateDate = Instant.ofEpochMilli(utcTimeMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            return !candidateDate.isBefore(minLocalDate)
        }

        override fun isSelectableYear(year: Int): Boolean = year >= minLocalDate.year
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialUtcMillis,
        selectableDates = selectableDates
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            color = with(MaterialTheme) {
                if (isQRAlarmTheme) colorScheme.surfaceContainerHighest
                else colorScheme.surfaceContainerHigh
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val datePickerColors = if (MaterialTheme.isQRAlarmTheme) {
                    DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.White,
                        subheadContentColor = Color.White,
                        navigationContentColor = Color.White,
                        yearContentColor = Color.White,
                        currentYearContentColor = Color.White,
                        selectedYearContainerColor = Jacarta,
                        selectedYearContentColor = Color.White,
                        dayContentColor = Color.White,
                        todayContentColor = Color.White,
                        todayDateBorderColor = Jacarta,
                        selectedDayContainerColor = Jacarta,
                        selectedDayContentColor = Color.White,
                        disabledDayContentColor = Color.White.copy(alpha = 0.3f)
                    )
                } else DatePickerDefaults.colors()

                DatePicker(
                    state = datePickerState,
                    colors = datePickerColors,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier.padding(top = MaterialTheme.space.medium)
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
                            color = with(MaterialTheme) {
                                if (isQRAlarmTheme) colorScheme.onSurface else Color.Unspecified
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                    val buttonColors = if (MaterialTheme.isQRAlarmTheme) {
                        ButtonDefaults.buttonColors(
                            containerColor = Jacarta,
                            contentColor = Color.White
                        )
                    } else ButtonDefaults.buttonColors()

                    Button(
                        onClick = {
                            val selectedUtcMillis = datePickerState.selectedDateMillis
                                ?: initialUtcMillis
                            val selectedLocalDate = Instant.ofEpochMilli(selectedUtcMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            val selectedLocalMillis = selectedLocalDate
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                            onDateConfirmed(selectedLocalMillis)
                        },
                        colors = buttonColors,
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
private fun AlarmDatePickerDialogPreview() {
    QRAlarmTheme {
        AlarmDatePickerDialog(
            initialDateInMillis = System.currentTimeMillis(),
            alarmHourOfDay = 8,
            alarmMinute = 30,
            onDismissRequest = {},
            onDateConfirmed = {}
        )
    }
}
