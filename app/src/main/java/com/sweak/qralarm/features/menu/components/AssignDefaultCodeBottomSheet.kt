package com.sweak.qralarm.features.menu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.add_edit_alarm.components.ComboBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignDefaultCodeBottomSheet(
    onScanCodeClicked: () -> Unit,
    availableCodes: List<String>,
    shouldAllowCodeClearance: Boolean,
    onChooseCodeFromList: (code: String) -> Unit,
    onDismissRequest: () -> Unit,
    onClearCodeClicked: () -> Unit
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
                text = stringResource(R.string.default_alarm_code),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            )

            Text(
                text = stringResource(R.string.default_alarm_code_description),
                style = MaterialTheme.typography.bodyLarge,
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
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(
                                top = MaterialTheme.space.mediumLarge,
                                bottom = MaterialTheme.space.medium
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

            if (shouldAllowCodeClearance) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.space.large)
                    )

                    OutlinedButton(
                        onClick = onClearCodeClicked,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(MaterialTheme.space.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = QRAlarmIcons.Delete,
                                contentDescription = stringResource(
                                    R.string.content_description_delete_icon
                                ),
                                tint = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = stringResource(R.string.clear_default_code),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AssignDefaultCodeBottomSheetPreview() {
    QRAlarmTheme {
        AssignDefaultCodeBottomSheet(
            onScanCodeClicked = {},
            availableCodes = listOf("StopAlarm", "472839472890421341"),
            shouldAllowCodeClearance = true,
            onChooseCodeFromList = {},
            onDismissRequest = {},
            onClearCodeClicked = {}
        )
    }
}