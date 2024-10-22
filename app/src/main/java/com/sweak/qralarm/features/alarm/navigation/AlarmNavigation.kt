package com.sweak.qralarm.features.alarm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.alarm.AlarmScreen

const val ALARM_SCREEN_ROUTE = "alarmScreen"
const val ID_OF_ALARM = "idOfAlarm"

fun NavGraphBuilder.alarmScreen(
    onStopAlarm: () -> Unit,
    onRequestCodeScan: () -> Unit
) {
    composable(
        route = "$ALARM_SCREEN_ROUTE/{$ID_OF_ALARM}",
        arguments = listOf(
            navArgument(ID_OF_ALARM) {
                type = NavType.LongType
            }
        )
    ) {
        AlarmScreen(
            onStopAlarm = onStopAlarm,
            onRequestCodeScan = onRequestCodeScan
        )
    }
}