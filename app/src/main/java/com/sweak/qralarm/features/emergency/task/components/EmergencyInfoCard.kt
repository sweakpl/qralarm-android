package com.sweak.qralarm.features.emergency.task.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun EmergencyInfoCard(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    Card(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.mediumLarge)
        ) {
            Icon(
                imageVector = QRAlarmIcons.Emergency,
                contentDescription =
                    stringResource(R.string.content_description_emergency_icon),
                modifier = Modifier.size(size = MaterialTheme.space.xxLarge)
            )

            Text(
                text = stringResource(R.string.emergency),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .padding(
                        top = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.medium
                    )
            )

            Text(
                text = stringResource(R.string.emergency_task_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MaterialTheme.space.large)
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.start))
            }
        }
    }
}

@Preview
@Composable
private fun EmergencyInfoCardPreview() {
    QRAlarmTheme {
        EmergencyInfoCard(
            onStartClick = {}
        )
    }
}