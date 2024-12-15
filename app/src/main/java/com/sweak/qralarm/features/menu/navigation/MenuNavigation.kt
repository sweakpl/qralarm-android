package com.sweak.qralarm.features.menu.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.menu.MenuScreen

const val MENU_SCREEN_ROUTE = "menuScreen"

fun NavController.navigateToMenu() = navigate(MENU_SCREEN_ROUTE)

fun NavGraphBuilder.menuScreen(
    onBackClicked: () -> Unit,
    onIntroductionClicked: () -> Unit,
    onOptimizationGuideClicked: () -> Unit,
    onQRAlarmProClicked: () -> Unit,
    onRateQRAlarmClicked: () -> Unit
) {
    composable(route = MENU_SCREEN_ROUTE) {
        MenuScreen(
            onBackClicked = onBackClicked,
            onIntroductionClicked = onIntroductionClicked,
            onOptimizationGuideClicked = onOptimizationGuideClicked,
            onQRAlarmProClicked = onQRAlarmProClicked,
            onRateQRAlarmClicked = onRateQRAlarmClicked
        )
    }
}