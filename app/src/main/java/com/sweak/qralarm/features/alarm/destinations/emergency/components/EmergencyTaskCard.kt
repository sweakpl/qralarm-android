package com.sweak.qralarm.features.alarm.destinations.emergency.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun EmergencyTaskCard(
    modifier: Modifier = Modifier,
    targetValue: Int,
    currentValue: Int,
    remainingMatches: Int,
    onValueChanged: (Int) -> Unit,
    onValueSelected: () -> Unit
) {
    QRAlarmCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.mediumLarge)
        ) {
            Row(
                modifier = Modifier.padding(bottom = MaterialTheme.space.large)
            ) {
                Text(
                    text = "Target:",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
                )

                Text(
                    text = targetValue.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = currentValue.toString(),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
            )

            Slider(
                value = currentValue.toFloat(),
                valueRange = 0f..100f,
                steps = 99,
                onValueChange = {
                    onValueChanged(it.toInt())
                },
                onValueChangeFinished = onValueSelected,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    activeTickColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onTertiary,
                    inactiveTickColor = MaterialTheme.colorScheme.onTertiary
                )
            )

            Row(
                modifier = Modifier.padding(top = MaterialTheme.space.large)
            ) {
                Text(
                    text = "Remaining:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
                )

                Text(
                    text = remainingMatches.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun EmergencyTaskCardPreview() {
    QRAlarmTheme {
        EmergencyTaskCard(
            targetValue = 100,
            currentValue = 50,
            remainingMatches = 5,
            onValueChanged = {},
            onValueSelected = {}
        )
    }
}