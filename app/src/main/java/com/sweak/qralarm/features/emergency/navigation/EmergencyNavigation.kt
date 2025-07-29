package com.sweak.qralarm.features.emergency.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.emergency.EmergencyScreen

const val EMERGENCY_SCREEN_ROUTE = "emergencyScreen"
const val ID_OF_ALARM_TO_CANCEL = "idOfAlarmToCancel"

fun NavGraphBuilder.emergencyScreen(
    onCloseClicked: () -> Unit,
    onEmergencyTaskCompleted: () -> Unit
) {
    composable(
        route = "$EMERGENCY_SCREEN_ROUTE/{$ID_OF_ALARM_TO_CANCEL}",
        arguments = listOf(
            navArgument(ID_OF_ALARM_TO_CANCEL) {
                type = NavType.LongType
            }
        )
    ) {
        EmergencyScreen(
            onCloseClicked = onCloseClicked,
            onEmergencyTaskCompleted = onEmergencyTaskCompleted
        )
    }
}

fun NavController.navigateToEmergencyScreen(
    alarmIdToCancel: Long = 0
) = navigate(
    route = "$EMERGENCY_SCREEN_ROUTE/$alarmIdToCancel"
)