package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseSnoozeConfigurationBottomSheet(
    initialAlarmSnoozeMode: Alarm.SnoozeMode,
    availableSnoozeNumbers: List<Int>,
    availableSnoozeDurationsInMinutes: List<Int>,
    onDismissRequest: (newAlarmSnoozeMode: Alarm.SnoozeMode) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedNumberOfSnoozes by remember {
        mutableIntStateOf(initialAlarmSnoozeMode.numberOfSnoozes)
    }
    var selectedSnoozeDurationInMinutes by remember {
        mutableIntStateOf(initialAlarmSnoozeMode.snoozeDurationInMinutes)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest(
                Alarm.SnoozeMode(
                    numberOfSnoozes = selectedNumberOfSnoozes,
                    snoozeDurationInMinutes = selectedSnoozeDurationInMinutes
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
                text = stringResource(R.string.snooze),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Text(
                text = stringResource(R.string.number_of_snoozes),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            )

            ComboBox(
                menuItems = availableSnoozeNumbers.map { snoozeNumber ->
                    pluralStringResource(
                        R.plurals.number_of_snoozes_plural,
                        snoozeNumber,
                        snoozeNumber
                    )
                },
                selectedIndex = availableSnoozeNumbers.indexOf(selectedNumberOfSnoozes),
                onMenuItemClick = { index ->
                    selectedNumberOfSnoozes = availableSnoozeNumbers[index]
                }
            )

            AnimatedVisibility(visible = selectedNumberOfSnoozes != 0) {
                Column {
                    Text(
                        text = stringResource(R.string.snooze_duration),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(
                                top = MaterialTheme.space.mediumLarge,
                                bottom = MaterialTheme.space.small
                            )
                    )

                    ComboBox(
                        menuItems = availableSnoozeDurationsInMinutes.map { snoozeDuration ->
                            pluralStringResource(
                                R.plurals.snooze_duration_plural,
                                snoozeDuration,
                                snoozeDuration
                            )
                        },
                        selectedIndex = availableSnoozeDurationsInMinutes.indexOf(
                            selectedSnoozeDurationInMinutes
                        ),
                        onMenuItemClick = { index ->
                            selectedSnoozeDurationInMinutes =
                                availableSnoozeDurationsInMinutes[index]
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChooseSnoozeConfigurationBottomSheetPreview() {
    QRAlarmTheme {
        ChooseSnoozeConfigurationBottomSheet(
            initialAlarmSnoozeMode = Alarm.SnoozeMode(3, 10),
            availableSnoozeNumbers = listOf(0, 1, 2, 3),
            availableSnoozeDurationsInMinutes = listOf(2, 3, 5, 10, 15, 20),
            onDismissRequest = {}
        )
    }
}