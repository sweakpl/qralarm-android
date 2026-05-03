package com.sweak.qralarm.features.onboarding.oversleeping_problem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun SoundsFamiliarCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(MaterialTheme.space.medium)) {
            Text(
                text = stringResource(R.string.sounds_familiar),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            )
            Text(
                text = stringResource(R.string.sounds_familiar_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
private fun SoundsFamiliarCardPreview() {
    QRAlarmTheme {
        SoundsFamiliarCard(modifier = Modifier.padding(MaterialTheme.space.medium))
    }
}
