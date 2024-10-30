package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.shortName
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChooseAlarmRepeatingScheduleBottomSheet(
    initialAlarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper,
    onDismissRequest: (newAlarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAlarmRepeatingMode by remember {
        mutableStateOf(initialAlarmRepeatingScheduleWrapper.alarmRepeatingMode)
    }
    val selectedAlarmDaysOfWeek = remember {
        mutableStateListOf(*initialAlarmRepeatingScheduleWrapper.alarmDaysOfWeek.toTypedArray())
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (selectedAlarmRepeatingMode == AlarmRepeatingMode.CUSTOM &&
                selectedAlarmDaysOfWeek.isEmpty()
            ) {
                selectedAlarmRepeatingMode = AlarmRepeatingMode.ONLY_ONCE
            }

            onDismissRequest(
                AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = selectedAlarmRepeatingMode,
                    alarmDaysOfWeek = selectedAlarmDaysOfWeek.sortedBy { it.value }
                )
            )
        },
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.mediumLarge,
                    end = MaterialTheme.space.mediumLarge,
                    bottom = MaterialTheme.space.xLarge
                )
        ) {
            Text(
                text = stringResource(R.string.repeating),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
                modifier = Modifier.selectableGroup()
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.ONLY_ONCE,
                            onClick = { selectedAlarmRepeatingMode = AlarmRepeatingMode.ONLY_ONCE },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.ONLY_ONCE,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = stringResource(R.string.only_once),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = MaterialTheme.space.medium)
                    )
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.MON_FRI,
                            onClick = { selectedAlarmRepeatingMode = AlarmRepeatingMode.MON_FRI },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.MON_FRI,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = DayOfWeek.MONDAY.shortName() + " - " + DayOfWeek.FRIDAY.shortName(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = MaterialTheme.space.medium)
                    )
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.SAT_SUN,
                            onClick = { selectedAlarmRepeatingMode = AlarmRepeatingMode.SAT_SUN },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.SAT_SUN,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = DayOfWeek.SATURDAY.shortName() + ", " + DayOfWeek.SUNDAY.shortName(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = MaterialTheme.space.medium)
                    )
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.EVERYDAY,
                            onClick = { selectedAlarmRepeatingMode = AlarmRepeatingMode.EVERYDAY },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.EVERYDAY,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = stringResource(R.string.everyday),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = MaterialTheme.space.medium)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.CUSTOM,
                                onClick = { selectedAlarmRepeatingMode = AlarmRepeatingMode.CUSTOM },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = selectedAlarmRepeatingMode == AlarmRepeatingMode.CUSTOM,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.secondary,
                                unselectedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = stringResource(R.string.custom),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = MaterialTheme.space.medium)
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedAlarmRepeatingMode == AlarmRepeatingMode.CUSTOM
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
                            modifier = Modifier.padding(top = MaterialTheme.space.medium)
                        ) {
                            DayOfWeek.entries.forEach { dayOfWeek ->
                                FilterChip(
                                    selected = dayOfWeek in selectedAlarmDaysOfWeek,
                                    onClick = {
                                        if (dayOfWeek in selectedAlarmDaysOfWeek) {
                                            selectedAlarmDaysOfWeek.remove(dayOfWeek)
                                        } else {
                                            selectedAlarmDaysOfWeek.add(dayOfWeek)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = dayOfWeek.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.getDefault()
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = dayOfWeek in selectedAlarmDaysOfWeek,
                                        borderColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChooseAlarmRepeatingScheduleBottomSheetPreview() {
    QRAlarmTheme {
        ChooseAlarmRepeatingScheduleBottomSheet(
            initialAlarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(
                alarmRepeatingMode = AlarmRepeatingMode.ONLY_ONCE,
                alarmDaysOfWeek = listOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)
            ),
            onDismissRequest = {}
        )
    }
}