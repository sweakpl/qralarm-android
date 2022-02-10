package com.sweak.qralarm.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                            MaterialTheme.colors.secondary,
                            MaterialTheme.colors.secondaryVariant
                        )
                    )
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h1,
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
                Button(
                    onClick = onNegativeClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
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
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
                Button(
                    onClick = onPositiveClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
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
                        modifier = Modifier.padding(MaterialTheme.space.extraSmall)
                    )
                }
            }
        }
    }
}