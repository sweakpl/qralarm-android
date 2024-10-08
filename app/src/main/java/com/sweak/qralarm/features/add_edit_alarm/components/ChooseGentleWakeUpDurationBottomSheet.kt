package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseGentleWakeUpDurationBottomSheet(
    initialGentleWakeUpDurationInSeconds: Int,
    availableGentleWakeUpDurationsInSeconds: List<Int>,
    onDismissRequest: (newGentleWakeUpDurationInSeconds: Int) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedGentleWakeUpDurationInSeconds by remember {
        mutableIntStateOf(initialGentleWakeUpDurationInSeconds)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(selectedGentleWakeUpDurationInSeconds) },
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
                text = stringResource(R.string.gentle_wake_up),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
                modifier = Modifier.selectableGroup()
            ) {
                availableGentleWakeUpDurationsInSeconds.forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedGentleWakeUpDurationInSeconds == duration,
                                onClick = { selectedGentleWakeUpDurationInSeconds = duration },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = selectedGentleWakeUpDurationInSeconds == duration,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.secondary,
                                unselectedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = if (duration != 0) {
                                pluralStringResource(
                                    R.plurals.number_of_seconds_plural,
                                    duration,
                                    duration
                                )
                            } else {
                                stringResource(R.string.disabled)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = MaterialTheme.space.medium)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChooseGentleWakeUpDurationBottomSheetPreview() {
    QRAlarmTheme {
        ChooseGentleWakeUpDurationBottomSheet(
            initialGentleWakeUpDurationInSeconds = 30,
            availableGentleWakeUpDurationsInSeconds = listOf(60, 30, 10, 0),
            onDismissRequest = {}
        )
    }
}