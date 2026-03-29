package com.sweak.qralarm.features.onboarding.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun WelcomeScreen(
    onGetStartedClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac)))
                .padding(paddingValues = paddingValues)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.space.medium)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.welcome_to_qralarm),
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = MaterialTheme.space.large)
                    )

                    Image(
                        painter = painterResource(R.drawable.img_welcome),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.get_out_of_bed_get_ahead),
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = MaterialTheme.space.large)
                    )
                }
            }

            Button(
                onClick = onGetStartedClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            ) {
                Text(
                    text = stringResource(R.string.get_started),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    QRAlarmTheme {
        WelcomeScreen(onGetStartedClicked = {})
    }
}
