package com.sweak.qralarm.features.alarm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.alarm.AlarmScreen

const val ALARM_SCREEN_ROUTE = "alarmScreen"
const val ID_OF_ALARM = "idOfAlarm"
const val IS_TRANSIENT = "isTransient"

fun NavGraphBuilder.alarmScreen(
    onStopAlarm: () -> Unit,
    onRequestCodeScan: () -> Unit,
    onSnoozeAlarm: () -> Unit
) {
    composable(
        route = "$ALARM_SCREEN_ROUTE/{$ID_OF_ALARM}/{$IS_TRANSIENT}",
        arguments = listOf(
            navArgument(ID_OF_ALARM) {
                type = NavType.LongType
            },
            navArgument(IS_TRANSIENT) {
                type = NavType.BoolType
            }
        )
    ) {
        AlarmScreen(
            onStopAlarm = onStopAlarm,
            onRequestCodeScan = onRequestCodeScan,
            onSnoozeAlarm = onSnoozeAlarm
        )
    }
}