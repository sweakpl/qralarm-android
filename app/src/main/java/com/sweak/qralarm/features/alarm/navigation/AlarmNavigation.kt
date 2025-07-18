package com.sweak.qralarm.features.alarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.sweak.qralarm.features.alarm.destinations.alarm.AlarmScreen
import com.sweak.qralarm.features.alarm.destinations.emergency.EmergencyScreen

const val ALARM_FLOW_ROUTE = "alarmFlow"
const val ID_OF_ALARM = "idOfAlarm"
const val IS_TRANSIENT = "isTransient"

const val ALARM_SCREEN_ROUTE = "alarmScreen"

const val EMERGENCY_SCREEN_ROUTE = "emergencyScreen"

fun NavGraphBuilder.alarmFlow(
    navController: NavController,
    onStopAlarm: () -> Unit,
    onRequestCodeScan: () -> Unit,
    onSnoozeAlarm: () -> Unit
) {
    var lastNavigateUpTime = 0L

    navigation(
        route = "$ALARM_FLOW_ROUTE/{$ID_OF_ALARM}/{$IS_TRANSIENT}",
        startDestination = ALARM_SCREEN_ROUTE,
        arguments = listOf(
            navArgument(ID_OF_ALARM) {
                type = NavType.LongType
            },
            navArgument(IS_TRANSIENT) {
                type = NavType.BoolType
            }
        )
    ) {
        composable(route = ALARM_SCREEN_ROUTE) {
            AlarmScreen(
                onStopAlarm = onStopAlarm,
                onRequestCodeScan = onRequestCodeScan,
                onSnoozeAlarm = onSnoozeAlarm,
                onEmergencyClicked = {
                    navController.navigate(route = EMERGENCY_SCREEN_ROUTE)
                }
            )
        }

        composable(route = EMERGENCY_SCREEN_ROUTE) {
             EmergencyScreen(
                 onCloseClicked = {
                     val currentTimeMillis = System.currentTimeMillis()

                     // Prevent excessive back navigation (due to double close press).
                     // We allow navigating up only if at least 3 seconds have passed since
                     // the last navigation:
                     if (lastNavigateUpTime + 3000L <= currentTimeMillis) {
                         lastNavigateUpTime = currentTimeMillis
                         navController.navigateUp()
                     }
                 },
                 onEmergencyTaskCompleted = onStopAlarm
             )
        }
    }
}