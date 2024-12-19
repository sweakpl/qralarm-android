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
fun ChooseTemporaryMuteDurationBottomSheet(
    initialTemporaryMuteDurationInSeconds: Int,
    availableTemporaryMuteDurationsInSeconds: List<Int>,
    onDismissRequest: (newTemporaryMuteDurationInSeconds: Int) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTemporaryMuteDurationInSeconds by remember {
        mutableIntStateOf(initialTemporaryMuteDurationInSeconds)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(selectedTemporaryMuteDurationInSeconds) },
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
                text = stringResource(R.string.temporary_mute),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
                modifier = Modifier.selectableGroup()
            ) {
                availableTemporaryMuteDurationsInSeconds.forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedTemporaryMuteDurationInSeconds == duration,
                                onClick = { selectedTemporaryMuteDurationInSeconds = duration },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = selectedTemporaryMuteDurationInSeconds == duration,
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
private fun ChooseTemporaryMuteDurationBottomSheetPreview() {
    QRAlarmTheme {
        ChooseTemporaryMuteDurationBottomSheet(
            initialTemporaryMuteDurationInSeconds = 15,
            availableTemporaryMuteDurationsInSeconds = listOf(60, 45, 30, 15, 0),
            onDismissRequest = {}
        )
    }
}