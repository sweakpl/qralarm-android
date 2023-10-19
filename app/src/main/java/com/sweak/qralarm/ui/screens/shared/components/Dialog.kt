package com.sweak.qralarm.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.ui.theme.space

@Composable
fun Dialog(
    onDismissRequest: () -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String
) {
    Dialog(
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
                text = title,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(
                    MaterialTheme.space.medium,
                    MaterialTheme.space.medium,
                    MaterialTheme.space.medium,
                    MaterialTheme.space.small
                )
            )
            Text(
                text = message,
                modifier = Modifier.padding(
                    MaterialTheme.space.medium,
                    MaterialTheme.space.small,
                    MaterialTheme.space.medium,
                    MaterialTheme.space.small
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onNegativeClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            MaterialTheme.space.medium,
                            MaterialTheme.space.small,
                            MaterialTheme.space.small,
                            MaterialTheme.space.medium
                        )
                ) {
                    Text(
                        text = negativeButtonText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
                Button(
                    onClick = onPositiveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            MaterialTheme.space.small,
                            MaterialTheme.space.small,
                            MaterialTheme.space.medium,
                            MaterialTheme.space.medium
                        )
                ) {
                    Text(
                        text = positiveButtonText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
            }
        }
    }
}