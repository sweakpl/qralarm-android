package com.sweak.qralarm.features.introduction.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.introduction.IntroductionScreen

const val INTRODUCTION_SCREEN_ROUTE = "introductionScreen"

fun NavGraphBuilder.introductionScreen(
    onContinueClicked: () -> Unit
) {
    composable(route = INTRODUCTION_SCREEN_ROUTE) {
        IntroductionScreen(
            onContinueClicked = onContinueClicked
        )
    }
}