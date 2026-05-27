package com.sweak.qralarm.features.codes_management.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.model.Code

@Composable
fun DefaultCodeCard(
    defaultCode: Code?,
    isPickingDefault: Boolean,
    hasOtherCodes: Boolean,
    onScanClicked: () -> Unit,
    onPickExistingClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onEditClicked: (Code) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .alpha(if (isPickingDefault) 0.5f else 1f)
            .then(
                if (isPickingDefault)
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                    .changes.forEach { it.consume() }
                            }
                        }
                    }
                else Modifier
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
            modifier = Modifier.padding(MaterialTheme.space.medium)
        ) {
            AnimatedContent(
                targetState = defaultCode,
                contentKey = { it != null },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "defaultCodeContent"
            ) { code ->
                if (code != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small)
                    ) {
                        CodeRow(
                            code = code,
                            trailingIcon = QRAlarmIcons.Edit,
                            trailingIconContentDescription = stringResource(
                                R.string.content_description_edit_icon
                            ),
                            onTrailingIconClicked = { onEditClicked(code) }
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = LocalContentColor.current
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small)
                    ) {
                        Text(
                            text = stringResource(R.string.no_default_code_yet),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.no_default_code_yet_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            val isQRAlarmTheme = MaterialTheme.isQRAlarmTheme

            Button(
                onClick = onScanClicked,
                colors = if (isQRAlarmTheme)
                    ButtonDefaults.buttonColors(containerColor = Jacarta)
                else
                    ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.scan_code))
            }

            if (hasOtherCodes) {
                OutlinedButton(
                    onClick = onPickExistingClicked,
                    colors = if (isQRAlarmTheme)
                        ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    else
                        ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(
                        1.dp,
                        if (isQRAlarmTheme) Color.White else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.pick_existing))
                }
            }

            if (defaultCode != null) {
                OutlinedButton(
                    onClick = onClearClicked,
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
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
