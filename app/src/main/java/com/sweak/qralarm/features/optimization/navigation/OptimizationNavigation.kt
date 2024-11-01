package com.sweak.qralarm.features.optimization.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.optimization.OptimizationScreen

const val OPTIMIZATION_SCREEN_ROUTE = "optimizationScreen"

fun NavController.navigateToOptimization() = navigate(OPTIMIZATION_SCREEN_ROUTE)

fun NavGraphBuilder.optimizationScreen(
    onBackClicked: () -> Unit
) {
    composable(route = OPTIMIZATION_SCREEN_ROUTE) {
        OptimizationScreen(
            onBackClicked = onBackClicked
        )
    }
}