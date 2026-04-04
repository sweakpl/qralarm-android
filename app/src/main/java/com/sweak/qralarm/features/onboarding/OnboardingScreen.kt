package com.sweak.qralarm.features.onboarding

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sweak.qralarm.core.navigation.Navigator
import com.sweak.qralarm.core.navigation.rememberNavigationState
import com.sweak.qralarm.core.navigation.toEntries
import com.sweak.qralarm.features.onboarding.introduction.IntroductionScreen
import com.sweak.qralarm.features.onboarding.permissions.PermissionsScreen
import com.sweak.qralarm.features.onboarding.social_proof.SocialProofScreen
import com.sweak.qralarm.features.onboarding.welcome.WelcomeScreen

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = WelcomeRoute,
        topLevelRoutes = setOf(WelcomeRoute)
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val onboardingEntryProvider = entryProvider {
        entry<WelcomeRoute> {
            WelcomeScreen(
                onGetStartedClicked = { navigator.navigate(SocialProofRoute) }
            )
        }
        entry<SocialProofRoute> {
            SocialProofScreen(
                onNextStepClicked = { navigator.navigate(IntroductionRoute) }
            )
        }
        entry<IntroductionRoute> {
            IntroductionScreen(
                onContinueClicked = { navigator.navigate(PermissionsRoute) }
            )
        }
        entry<PermissionsRoute> {
            PermissionsScreen(
                onOnboardingFinished = onOnboardingFinished
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(
            entryProvider = onboardingEntryProvider
        ),
        onBack = { navigator.goBack() },
        transitionSpec = { fadeIn() togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith fadeOut() },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith fadeOut() },
        modifier = modifier
    )
}
