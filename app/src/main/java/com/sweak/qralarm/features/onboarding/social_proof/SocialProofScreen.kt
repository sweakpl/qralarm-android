package com.sweak.qralarm.features.onboarding.social_proof

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.social_proof.components.ReviewsCarousel

@Composable
fun SocialProofScreen(
    onNextStepClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isButtonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000L)
        isButtonVisible = true
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac)))
                .padding(paddingValues = paddingValues)
        ) {
            Text(
                text = stringResource(R.string.thousands_of_mornings_changed),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.xLarge,
                        end = MaterialTheme.space.medium
                    )
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ReviewsCarousel()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
                    .height(
                        MaterialTheme.space.mediumLarge + MaterialTheme.space.medium
                    )
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isButtonVisible,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(
                        onClick = onNextStepClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.next_step),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SocialProofScreenPreview() {
    QRAlarmTheme {
        SocialProofScreen(onNextStepClicked = {})
    }
}
