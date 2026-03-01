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
    onEmergencyTaskSettingsClicked: () -> Unit,
    onQRAlarmProClicked: () -> Unit,
    onRateQRAlarmClicked: () -> Unit,
    onScanDefaultCodeClicked: () -> Unit,
    onThemeClicked: () -> Unit
) {
    composable(route = MENU_SCREEN_ROUTE) {
        MenuScreen(
            onBackClicked = onBackClicked,
            onIntroductionClicked = onIntroductionClicked,
            onOptimizationGuideClicked = onOptimizationGuideClicked,
            onEmergencyTaskSettingsClicked = onEmergencyTaskSettingsClicked,
            onQRAlarmProClicked = onQRAlarmProClicked,
            onRateQRAlarmClicked = onRateQRAlarmClicked,
            onScanDefaultCodeClicked = onScanDefaultCodeClicked,
            onThemeClicked = onThemeClicked
        )
    }
}