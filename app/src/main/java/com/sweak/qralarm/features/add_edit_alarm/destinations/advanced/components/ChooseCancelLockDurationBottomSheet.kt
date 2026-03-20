package com.sweak.qralarm.features.add_edit_alarm.destinations.advanced.components

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
import com.sweak.qralarm.core.designsystem.component.QRAlarmRadioButton
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCancelLockDurationBottomSheet(
    initialCancelLockDurationInHours: Int,
    availableCancelLockDurationsInHours: List<Int>,
    onDismissRequest: (newCancelLockDurationInHours: Int) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCancelLockDurationInHours by remember {
        mutableIntStateOf(initialCancelLockDurationInHours)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(selectedCancelLockDurationInHours) },
        sheetState = modalBottomSheetState
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
                text = stringResource(R.string.cancel_lock),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
                modifier = Modifier.selectableGroup()
            ) {
                availableCancelLockDurationsInHours.forEach { durationInHours ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedCancelLockDurationInHours == durationInHours,
                                onClick = {
                                    selectedCancelLockDurationInHours = durationInHours
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        QRAlarmRadioButton(
                            selected = selectedCancelLockDurationInHours == durationInHours,
                            onClick = null
                        )

                        Text(
                            text = if (durationInHours != 0) {
                                pluralStringResource(
                                    R.plurals.hours,
                                    durationInHours,
                                    durationInHours
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
private fun ChooseCancelLockDurationBottomSheetPreview() {
    QRAlarmTheme {
        ChooseCancelLockDurationBottomSheet(
            initialCancelLockDurationInHours = 1,
            availableCancelLockDurationsInHours = listOf(3, 2, 1, 0),
            onDismissRequest = {}
        )
    }
}
