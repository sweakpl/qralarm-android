package com.sweak.qralarm.features.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.home.HomeScreen

const val HOME_SCREEN_ROUTE = "homeScreen"

fun NavController.navigateToHome(navOptions: NavOptions) = navigate(
    route = HOME_SCREEN_ROUTE,
    navOptions = navOptions
)

fun NavGraphBuilder.homeScreen(
    onAddNewAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onMenuClicked: () -> Unit,
    onRedirectToScanner: (alarmId: Long) -> Unit,
    onGoToOptimizationClicked: () -> Unit
) {
    composable(route = HOME_SCREEN_ROUTE) {
        HomeScreen(
            onAddNewAlarm = onAddNewAlarm,
            onEditAlarm = onEditAlarm,
            onMenuClicked = onMenuClicked,
            onRedirectToScanner = onRedirectToScanner,
            onGoToOptimizationClicked = onGoToOptimizationClicked
        )
    }
}