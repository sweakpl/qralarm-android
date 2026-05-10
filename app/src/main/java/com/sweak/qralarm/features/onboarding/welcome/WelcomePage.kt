package com.sweak.qralarm.features.onboarding.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.welcome.components.RingingAlarmPhone

@Composable
fun WelcomePage(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val imageMaxHeight = maxHeight * 0.5f

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = MaterialTheme.space.medium,
                    top = MaterialTheme.space.mediumLarge,
                    end = MaterialTheme.space.medium,
                    bottom = MaterialTheme.space.small
                )
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = typography.displayMedium,
                textAlign = TextAlign.Center,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = typography.bodyLarge.fontSize,
                    maxFontSize = typography.displayMedium.fontSize
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = typography.bodyLarge,
                textAlign = TextAlign.Center,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = typography.labelLarge.fontSize,
                    maxFontSize = typography.bodyLarge.fontSize
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.space.mediumLarge)
                    .heightIn(max = imageMaxHeight)
                    .weight(1f)
            ) {
                RingingAlarmPhone(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
private fun WelcomePagePreview() {
    QRAlarmTheme {
        WelcomePage(modifier = Modifier.fillMaxSize())
    }
}
