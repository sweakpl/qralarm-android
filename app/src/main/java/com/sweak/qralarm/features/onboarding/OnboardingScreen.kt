package com.sweak.qralarm.features.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.how_it_works.HowItWorksPage
import com.sweak.qralarm.features.onboarding.oversleeping_problem.OversleepingProblemPage
import com.sweak.qralarm.features.onboarding.permissions.PermissionsPage
import com.sweak.qralarm.features.onboarding.social_proof.SocialProofPage
import com.sweak.qralarm.features.onboarding.welcome.WelcomePage
import kotlinx.coroutines.launch

private enum class OnboardingPage {
    WELCOME,
    OVERSLEEPING_PROBLEM,
    HOW_IT_WORKS,
    SOCIAL_PROOF,
    PERMISSIONS
}

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac)))
                .padding(paddingValues = paddingValues)
        ) {
            val pagerState = rememberPagerState(pageCount = { OnboardingPage.entries.size })
            val composableScope = rememberCoroutineScope()
            var areAllRequiredPermissionsHandled by remember { mutableStateOf(false) }

            BackHandler(enabled = pagerState.currentPage > OnboardingPage.WELCOME.ordinal) {
                composableScope.launch {
                    pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (OnboardingPage.entries[page]) {
                    OnboardingPage.WELCOME -> WelcomePage()
                    OnboardingPage.OVERSLEEPING_PROBLEM ->
                        OversleepingProblemPage(isActive = pagerState.currentPage == page)
                    OnboardingPage.HOW_IT_WORKS -> HowItWorksPage()
                    OnboardingPage.SOCIAL_PROOF ->
                        SocialProofPage(isActive = pagerState.currentPage == page)
                    OnboardingPage.PERMISSIONS -> PermissionsPage(
                        onLetsGoEnabledChange = { areAllRequiredPermissionsHandled = it }
                    )
                }
            }

            Row(modifier = Modifier.padding(vertical = MaterialTheme.space.medium)) {
                repeat(pagerState.pageCount) {
                    val color =
                        if (pagerState.currentPage == it) LocalContentColor.current
                        else LocalContentColor.current.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.space.small)
                            .clip(CircleShape)
                            .background(color)
                            .size(MaterialTheme.space.small)
                    )
                }
            }

            val currentPage = OnboardingPage.entries[pagerState.currentPage]

            val buttonTextRes = when (currentPage) {
                OnboardingPage.WELCOME -> R.string.get_started
                OnboardingPage.PERMISSIONS -> R.string.lets_go
                else -> R.string.next_step
            }

            Button(
                enabled = currentPage != OnboardingPage.PERMISSIONS || areAllRequiredPermissionsHandled,
                onClick = {
                    if (currentPage == OnboardingPage.PERMISSIONS) {
                        onOnboardingFinished()
                    } else {
                        composableScope.launch {
                            pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            ) {
                Text(
                    text = stringResource(buttonTextRes),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    QRAlarmTheme {
        OnboardingScreen(onOnboardingFinished = {})
    }
}
