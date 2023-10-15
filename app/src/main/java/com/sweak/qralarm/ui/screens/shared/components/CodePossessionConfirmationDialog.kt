package com.sweak.qralarm.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.space

@Composable
fun CodePossessionConfirmationDialog(
    onDoneClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val isCodePossessionCheckboxChecked = remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
        ) {
            Text(
                text = stringResource(R.string.do_you_have_code),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(MaterialTheme.space.medium)
            )

            Text(
                text = stringResource(R.string.make_sure_you_have_code),
                modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(MaterialTheme.space.medium)
            ) {
                Checkbox(
                    checked = isCodePossessionCheckboxChecked.value,
                    onCheckedChange = { isCodePossessionCheckboxChecked.value = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(R.string.yes_i_have_code),
                    modifier = Modifier.padding(start = MaterialTheme.space.small)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.medium,
                    )
            ) {
                TextButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = MaterialTheme.space.small)
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }

                Button(
                    onClick = onDoneClicked,
                    enabled = isCodePossessionCheckboxChecked.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor =
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = MaterialTheme.space.small)
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
            }
        }
    }
}