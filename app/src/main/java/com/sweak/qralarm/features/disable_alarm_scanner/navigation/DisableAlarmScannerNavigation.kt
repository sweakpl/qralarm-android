package com.sweak.qralarm.features.disable_alarm_scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.disable_alarm_scanner.DisableAlarmScannerScreen

const val DISABLE_ALARM_SCANNER_SCREEN_ROUTE = "disableAlarmScannerScreen"
const val ID_OF_ALARM = "idOfAlarm"
const val IS_DISABLING_BEFORE_ALARM_FIRED = "isDisablingBeforeAlarmFired"

fun NavController.navigateToDisableAlarmScanner(
    alarmId: Long = 0,
    isDisablingBeforeAlarmFired: Boolean = false
) = navigate(
    route = DISABLE_ALARM_SCANNER_SCREEN_ROUTE +
            "/$alarmId" +
            "/$isDisablingBeforeAlarmFired"
)

fun NavGraphBuilder.disableAlarmScannerScreen(
    onAlarmDisabled: (uriStringToTryToOpen: String?) -> Unit,
    onCloseClicked: () -> Unit
) {
    composable(
        route = DISABLE_ALARM_SCANNER_SCREEN_ROUTE +
                "/{$ID_OF_ALARM}" +
                "/{$IS_DISABLING_BEFORE_ALARM_FIRED}",
        arguments = listOf(
            navArgument(ID_OF_ALARM) {
                type = NavType.LongType
            },
            navArgument(IS_DISABLING_BEFORE_ALARM_FIRED) {
                type = NavType.BoolType
            }
        )
    ) {
        DisableAlarmScannerScreen(
            onAlarmDisabled = onAlarmDisabled,
            onCloseClicked = onCloseClicked
        )
    }
}