package com.sweak.qralarm.features.onboarding.how_it_works

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.Gold
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.how_it_works.components.StepsCard

@Composable
fun HowItWorksPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.space.medium)
    ) {
        Text(
            text = stringResource(R.string.the_cure_isnt_louder),
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = stringResource(R.string.its_further_away),
            style = MaterialTheme.typography.displaySmall,
            color = Gold,
            modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
        )

        Text(
            text = stringResource(R.string.how_it_works_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
        )

        StepsCard()
    }
}

@Preview
@Composable
private fun HowItWorksPagePreview() {
    QRAlarmTheme {
        HowItWorksPage(modifier = Modifier.fillMaxSize())
    }
}
