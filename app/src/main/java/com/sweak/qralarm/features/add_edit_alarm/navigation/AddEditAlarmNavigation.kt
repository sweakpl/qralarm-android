package com.sweak.qralarm.features.add_edit_alarm.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmScreen
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmViewModel

const val ADD_EDIT_ALARM_FLOW_ROUTE = "addEditAlarmFlow"
const val ADD_EDIT_ALARM_SCREEN_ROUTE = "addEditAlarmScreen"
const val ID_OF_ALARM_TO_EDIT = "idOfAlarmToEdit"

fun NavController.navigateToAddEditAlarm(
    alarmId: Long = 0
) = navigate(
    route = "$ADD_EDIT_ALARM_FLOW_ROUTE/$alarmId"
)

fun NavGraphBuilder.addEditAlarmFlow(
    navController: NavController,
    onCancelClicked: () -> Unit,
    onAlarmSaved: () -> Unit,
    onScanCustomCodeClicked: () -> Unit,
    onAlarmDeleted: () -> Unit,
    onRedirectToQRAlarmPro: () -> Unit
) {
    navigation(
        route = "$ADD_EDIT_ALARM_FLOW_ROUTE/{$ID_OF_ALARM_TO_EDIT}",
        startDestination = ADD_EDIT_ALARM_SCREEN_ROUTE,
        arguments = listOf(
            navArgument(ID_OF_ALARM_TO_EDIT) {
                type = NavType.LongType
            }
        )
    ) {
        composable(route = ADD_EDIT_ALARM_SCREEN_ROUTE) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry("$ADD_EDIT_ALARM_FLOW_ROUTE/{$ID_OF_ALARM_TO_EDIT}")
            }
            val addEditAlarmViewModel = hiltViewModel<AddEditAlarmViewModel>(parentEntry)

            AddEditAlarmScreen(
                addEditAlarmViewModel = addEditAlarmViewModel,
                onCancelClicked = onCancelClicked,
                onAlarmSaved = onAlarmSaved,
                onScanCustomCodeClicked = onScanCustomCodeClicked,
                onAlarmDeleted = onAlarmDeleted,
                onRedirectToQRAlarmPro = onRedirectToQRAlarmPro
            )
        }
    }
}