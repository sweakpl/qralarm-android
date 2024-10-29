package com.sweak.qralarm.features.introduction.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.introduction.IntroductionScreen

const val INTRODUCTION_SCREEN_ROUTE = "introductionScreen"
const val IS_LAUNCHED_FROM_MENU = "isLaunchedFromMenu"

fun NavController.navigateToIntroduction(
    isLaunchedFromMenu: Boolean
) = navigate(
    route = "$INTRODUCTION_SCREEN_ROUTE/$isLaunchedFromMenu"
)

fun NavGraphBuilder.introductionScreen(
    onContinueClicked: (wasLaunchedFromMenu: Boolean) -> Unit
) {
    composable(
        route = "$INTRODUCTION_SCREEN_ROUTE/{$IS_LAUNCHED_FROM_MENU}",
        arguments = listOf(
            navArgument(IS_LAUNCHED_FROM_MENU) {
                type = NavType.BoolType
            }
        )
    ) { backStackEntry ->
        IntroductionScreen(
            onContinueClicked = {
                backStackEntry.arguments?.getBoolean(IS_LAUNCHED_FROM_MENU)?.let {
                    onContinueClicked(it)
                }
            }
        )
    }
}
