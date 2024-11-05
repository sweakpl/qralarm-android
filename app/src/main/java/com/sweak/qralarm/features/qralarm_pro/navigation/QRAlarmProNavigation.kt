package com.sweak.qralarm.features.qralarm_pro.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.qralarm_pro.QRAlarmProScreen

const val QRALARM_PRO_SCREEN_ROUTE = "qralarmProScreen"

fun NavController.navigateToQRAlarmPro() = navigate(QRALARM_PRO_SCREEN_ROUTE)

fun NavGraphBuilder.qralarmProScreen(
    onNotNowClicked: () -> Unit
) {
    composable(route = QRALARM_PRO_SCREEN_ROUTE) {
        QRAlarmProScreen(
            onNotNowClicked = onNotNowClicked
        )
    }
}