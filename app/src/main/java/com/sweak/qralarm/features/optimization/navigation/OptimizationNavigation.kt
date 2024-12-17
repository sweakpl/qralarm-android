package com.sweak.qralarm.features.optimization.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.optimization.OptimizationScreen

const val OPTIMIZATION_SCREEN_ROUTE = "optimizationScreen"
const val IS_LAUNCHED_FROM_MENU = "isLaunchedFromMenu"

fun NavController.navigateToOptimization(
    isLaunchedFromMenu: Boolean = false
) = navigate(
    route = "$OPTIMIZATION_SCREEN_ROUTE/$isLaunchedFromMenu"
)

fun NavGraphBuilder.optimizationScreen(
    onBackClicked: () -> Unit
) {
    composable(
        route = "$OPTIMIZATION_SCREEN_ROUTE/{$IS_LAUNCHED_FROM_MENU}",
        arguments = listOf(
            navArgument(IS_LAUNCHED_FROM_MENU) {
                type = NavType.BoolType
            }
        )
    ) {
        OptimizationScreen(
            onBackClicked = onBackClicked
        )
    }
}