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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmRadioButton
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.getCancelLockDurationString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCancelLockDurationBottomSheet(
    initialCancelLockDurationInMinutes: Int,
    availableCancelLockDurationsInMinutes: List<Int>,
    onDismissRequest: (newCancelLockDurationInMinutes: Int) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCancelLockDurationInMinutes by remember {
        mutableIntStateOf(initialCancelLockDurationInMinutes)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(selectedCancelLockDurationInMinutes) },
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
                text = stringResource(R.string.cancellation_lock),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
                modifier = Modifier.selectableGroup()
            ) {
                availableCancelLockDurationsInMinutes.forEach { durationInMinutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedCancelLockDurationInMinutes == durationInMinutes,
                                onClick = {
                                    selectedCancelLockDurationInMinutes = durationInMinutes
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        QRAlarmRadioButton(
                            selected = selectedCancelLockDurationInMinutes == durationInMinutes,
                            onClick = null
                        )

                        Text(
                            text = getCancelLockDurationString(durationInMinutes),
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
            initialCancelLockDurationInMinutes = 60,
            availableCancelLockDurationsInMinutes = listOf(180, 120, 60, 30, 15, 0),
            onDismissRequest = {}
        )
    }
}
