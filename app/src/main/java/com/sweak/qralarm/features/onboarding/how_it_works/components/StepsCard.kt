package com.sweak.qralarm.features.onboarding.how_it_works.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.ButterflyBush
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun StepsCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.space.smallMedium))
            .background(ButterflyBush)
            .padding(MaterialTheme.space.medium)
    ) {
        HowItWorksStep(
            number = 1,
            titleRes = R.string.how_it_works_step_1_title,
            descRes = R.string.how_it_works_step_1_description
        )
        HorizontalDivider(
            color = LocalContentColor.current,
            modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
        )
        HowItWorksStep(
            number = 2,
            titleRes = R.string.how_it_works_step_2_title,
            descRes = R.string.how_it_works_step_2_description
        )
        HorizontalDivider(
            color = LocalContentColor.current,
            modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
        )
        HowItWorksStep(
            number = 3,
            titleRes = R.string.how_it_works_step_3_title,
            descRes = R.string.how_it_works_step_3_description
        )
    }
}

@Composable
private fun HowItWorksStep(
    number: Int,
    @StringRes titleRes: Int,
    @StringRes descRes: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MaterialTheme.space.large)
                .clip(CircleShape)
                .background(BlueZodiac)
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Column(modifier = Modifier.padding(start = MaterialTheme.space.smallMedium)) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
private fun StepsCardPreview() {
    QRAlarmTheme {
        StepsCard()
    }
}
