package com.sweak.qralarm.features.disable_alarm_scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.disable_alarm_scanner.DisableAlarmScannerScreen

const val DISABLE_ALARM_SCANNER_SCREEN_ROUTE = "disableAlarmScannerScreen"
const val ID_OF_ALARM = "idOfAlarm"

fun NavController.navigateToDisableAlarmScanner(
    alarmId: Long = 0
) = navigate(
    route = "$DISABLE_ALARM_SCANNER_SCREEN_ROUTE/$alarmId"
)

fun NavGraphBuilder.disableAlarmScannerScreen(
    onAlarmDisabled: () -> Unit
) {
    composable(
        route = "$DISABLE_ALARM_SCANNER_SCREEN_ROUTE/{$ID_OF_ALARM}",
        arguments = listOf(
            navArgument(ID_OF_ALARM) {
                type = NavType.LongType
            }
        )
    ) {
        DisableAlarmScannerScreen(
            onAlarmDisabled = onAlarmDisabled
        )
    }
}