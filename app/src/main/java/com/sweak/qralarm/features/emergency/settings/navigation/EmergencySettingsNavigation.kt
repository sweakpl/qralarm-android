package com.sweak.qralarm.features.emergency.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sweak.qralarm.features.emergency.settings.EmergencySettingsScreen
import com.sweak.qralarm.features.emergency.task.EmergencyScreen

const val EMERGENCY_SETTINGS_FLOW_ROUTE = "emergencySettingsFlow"

const val EMERGENCY_SETTINGS_SCREEN_ROUTE = "emergencySettingsScreen"
const val EMERGENCY_TASK_PREVIEW_SCREEN_ROUTE = "emergencyTaskPreviewScreen"

fun NavController.navigateToEmergencySettings() = navigate(EMERGENCY_SETTINGS_FLOW_ROUTE)

fun NavGraphBuilder.emergencySettingsFlow(
    navController: NavController,
    onBackClicked: () -> Unit
) {
    navigation(
        route = EMERGENCY_SETTINGS_FLOW_ROUTE,
        startDestination = EMERGENCY_SETTINGS_SCREEN_ROUTE
    ) {
        composable(route = EMERGENCY_SETTINGS_SCREEN_ROUTE) {
            EmergencySettingsScreen(
                onBackClicked = onBackClicked,
                onPreviewEmergencyTaskClicked = {
                    navController.navigate(EMERGENCY_TASK_PREVIEW_SCREEN_ROUTE)
                }
            )
        }

        composable(route = EMERGENCY_TASK_PREVIEW_SCREEN_ROUTE) {
            EmergencyScreen(
                onCloseClicked = {
                    navController.navigateUp()
                },
                onEmergencyTaskCompleted = {
                    navController.popBackStack()
                }
            )
        }
    }
}