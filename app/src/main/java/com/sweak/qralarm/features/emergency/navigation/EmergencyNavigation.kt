package com.sweak.qralarm.features.emergency.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.emergency.EmergencyScreen

const val EMERGENCY_SCREEN_ROUTE = "emergencyScreen"

fun NavGraphBuilder.emergencyScreen(
    onCloseClicked: () -> Unit,
    onEmergencyTaskCompleted: () -> Unit
) {
    composable(route = EMERGENCY_SCREEN_ROUTE) {
        EmergencyScreen(
            onCloseClicked = onCloseClicked,
            onEmergencyTaskCompleted = onEmergencyTaskCompleted
        )
    }
}

fun NavController.navigateToEmergencyScreen() = navigate(route = EMERGENCY_SCREEN_ROUTE)