package com.sweak.qralarm.features.rate.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.rate.RateScreen

const val RATE_SCREEN_ROUTE = "rateScreen"

fun NavController.navigateToRate() = navigate(RATE_SCREEN_ROUTE)

fun NavGraphBuilder.rateScreen(
    onExit: () -> Unit
) {
    composable(route = RATE_SCREEN_ROUTE) {
        RateScreen(onExit = onExit)
    }
}