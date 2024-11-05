package com.sweak.qralarm.features.add_edit_alarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmScreen

const val ADD_EDIT_ALARM_SCREEN_ROUTE = "addEditAlarmScreen"
const val ID_OF_ALARM_TO_EDIT = "idOfAlarmToEdit"

fun NavController.navigateToAddEditAlarm(
    alarmId: Long = 0
) = navigate(
    route = "$ADD_EDIT_ALARM_SCREEN_ROUTE/$alarmId"
)

fun NavGraphBuilder.addEditAlarmScreen(
    onCancelClicked: () -> Unit,
    onAlarmSaved: () -> Unit,
    onScanCustomCodeClicked: () -> Unit,
    onAlarmDeleted: () -> Unit,
    onRedirectToQRAlarmPro: () -> Unit
) {
    composable(
        route = "$ADD_EDIT_ALARM_SCREEN_ROUTE/{$ID_OF_ALARM_TO_EDIT}",
        arguments = listOf(
            navArgument(ID_OF_ALARM_TO_EDIT) {
                type = NavType.LongType
            }
        )
    ) {
        AddEditAlarmScreen(
            onCancelClicked = onCancelClicked,
            onAlarmSaved = onAlarmSaved,
            onScanCustomCodeClicked = onScanCustomCodeClicked,
            onAlarmDeleted = onAlarmDeleted,
            onRedirectToQRAlarmPro = onRedirectToQRAlarmPro
        )
    }
}