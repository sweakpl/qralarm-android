package com.sweak.qralarm.features.emergency.task.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmSlider
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.emergency.task.EmergencyScreenState

@Composable
fun EmergencyTaskCard(
    modifier: Modifier = Modifier,
    emergencyTaskConfig: EmergencyScreenState.EmergencyTaskConfig,
    onValueChanged: (Int) -> Unit,
    onValueSelected: () -> Unit
) {
    Card(modifier = modifier) {
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
                    text = stringResource(R.string.target_colon),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
                )

                Text(
                    text = emergencyTaskConfig.targetValue.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = emergencyTaskConfig.currentValue.toString(),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
            )

            QRAlarmSlider(
                enabled = !emergencyTaskConfig.isCompleted,
                value = emergencyTaskConfig.currentValue.toFloat(),
                valueRange = with(emergencyTaskConfig.valueRange) {
                    first.toFloat()..last.toFloat()
                },
                steps = with(emergencyTaskConfig.valueRange) { last - first },
                onValueChange = {
                    onValueChanged(it.toInt())
                },
                onValueChangeFinished = onValueSelected
            )

            Row(
                modifier = Modifier.padding(top = MaterialTheme.space.large)
            ) {
                Text(
                    text = stringResource(R.string.remaining_colon),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
                )

                Text(
                    text = emergencyTaskConfig.remainingMatches.toString(),
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
            emergencyTaskConfig = EmergencyScreenState.EmergencyTaskConfig(
                valueRange = 0..100,
                targetValue = 100,
                currentValue = 50,
                remainingMatches = 5
            ),
            onValueChanged = {},
            onValueSelected = {},
        )
    }
}