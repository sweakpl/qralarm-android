package com.sweak.qralarm.features.onboarding.oversleeping_problem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.oversleeping_problem.components.SoundsFamiliarCard
import com.sweak.qralarm.features.onboarding.oversleeping_problem.components.StatisticCard

@Composable
fun OversleepingProblemPage(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.space.medium)
    ) {
        StatisticCard(
            isActive = isActive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.space.mediumLarge)
        )
        SoundsFamiliarCard(
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview
@Composable
private fun OversleepingProblemPagePreview() {
    QRAlarmTheme {
        OversleepingProblemPage(
            isActive = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
