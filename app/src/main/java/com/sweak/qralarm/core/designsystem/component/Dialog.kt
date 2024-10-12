package com.sweak.qralarm.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun QRAlarmDialog(
    title: String,
    message: String? = null,
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    positiveButtonText: String,
    positiveButtonColor: Color = MaterialTheme.colorScheme.primary,
    onNegativeClick: (() -> Unit)? = null,
    negativeButtonText: String? = null
) {
    val onlyPositiveButton = negativeButtonText == null

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
            Column(modifier = Modifier.wrapContentSize()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium
                    )
                )

                message?.let {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.small,
                            end = MaterialTheme.space.medium
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = MaterialTheme.space.medium)
                ) {
                    if (!onlyPositiveButton) {
                        TextButton(
                            onClick = onNegativeClick ?: onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = negativeButtonText!!,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(MaterialTheme.space.medium))
                    }

                    Button(
                        onClick = onPositiveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = positiveButtonColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = positiveButtonText,
                            modifier = Modifier.padding(MaterialTheme.space.xSmall)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialogPreview() {
    QRAlarmTheme {
        QRAlarmDialog(
            title = "Example Dialog",
            message = "Example dialog's body is here.",
            onDismissRequest = { /* no-op */ },
            onPositiveClick = { /* no-op */ },
            positiveButtonText = "Yes",
            negativeButtonText = "No"
        )
    }
}