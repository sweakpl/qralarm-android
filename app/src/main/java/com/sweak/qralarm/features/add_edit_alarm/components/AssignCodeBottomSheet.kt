package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignCodeBottomSheet(
    onScanCodeClicked: () -> Unit,
    availableCodes: List<String>,
    onChooseCodeFromList: (code: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                text = stringResource(R.string.scan_your_own_code),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            )

            Button(
                onClick = onScanCodeClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.scan_code),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (availableCodes.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.or_use_already_used_code),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(
                                top = MaterialTheme.space.mediumLarge,
                                bottom = MaterialTheme.space.small
                            )
                    )

                    val context = LocalContext.current

                    ComboBox(
                        menuItems = listOf(
                            context.getString(R.string.click_to_choose),
                            *availableCodes.toTypedArray()
                        ),
                        selectedIndex = 0,
                        onMenuItemClick = { index ->
                            if (index != 0) {
                                availableCodes.getOrNull(index - 1)?.let {
                                    onChooseCodeFromList(it)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AssignCodeBottomSheetPreview() {
    QRAlarmTheme {
        AssignCodeBottomSheet(
            onScanCodeClicked = {},
            availableCodes = listOf("StopAlarm", "472839472890421341"),
            onChooseCodeFromList = {},
            onDismissRequest = {}
        )
    }
}