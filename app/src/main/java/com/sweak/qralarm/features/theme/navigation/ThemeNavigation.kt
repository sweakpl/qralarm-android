package com.sweak.qralarm.features.theme.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.theme.ThemeScreen

const val THEME_SCREEN_ROUTE = "themeScreen"

fun NavController.navigateToTheme() = navigate(THEME_SCREEN_ROUTE)

fun NavGraphBuilder.themeScreen(
    onBackClicked: () -> Unit,
    onGoToQRAlarmProCheckout: () -> Unit
) {
    composable(route = THEME_SCREEN_ROUTE) {
        ThemeScreen(
            onBackClicked = onBackClicked,
            onGoToQRAlarmProCheckout = onGoToQRAlarmProCheckout
        )
    }
}
