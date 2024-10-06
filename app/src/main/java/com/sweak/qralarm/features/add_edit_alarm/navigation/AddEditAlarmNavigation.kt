package com.sweak.qralarm.features.add_edit_alarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmScreen

const val ADD_EDIT_ALARM_SCREEN_ROUTE = "addEditAlarmScreen"

fun NavController.navigateToAddEditAlarm() = navigate(route = ADD_EDIT_ALARM_SCREEN_ROUTE)

fun NavGraphBuilder.addEditAlarmScreen(
    onCancelClicked: () -> Unit,
    onScanCustomCodeClicked: () -> Unit
) {
    composable(route = ADD_EDIT_ALARM_SCREEN_ROUTE) {
        AddEditAlarmScreen(
            onCancelClicked = onCancelClicked,
            onScanCustomCodeClicked = onScanCustomCodeClicked
        )
    }
}