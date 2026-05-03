package com.sweak.qralarm.features.onboarding.oversleeping_problem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.Gold
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun StatisticCard(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedPercent = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            animatedPercent.snapTo(0f)
            animatedPercent.animateTo(
                targetValue = 55f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        } else {
            animatedPercent.snapTo(0f)
        }
    }

    Card(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.space.mediumLarge,
                    vertical = MaterialTheme.space.large
                )
        ) {
            Text(
                text = "${animatedPercent.value.toInt()}%",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = Gold,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            )
            Text(
                text = stringResource(R.string.of_people_oversleep),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MaterialTheme.space.small)
            )
            Text(
                text = stringResource(R.string.survey_source),
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun StatisticCardPreview() {
    QRAlarmTheme {
        StatisticCard(
            isActive = true,
            modifier = Modifier.padding(MaterialTheme.space.medium)
        )
    }
}
