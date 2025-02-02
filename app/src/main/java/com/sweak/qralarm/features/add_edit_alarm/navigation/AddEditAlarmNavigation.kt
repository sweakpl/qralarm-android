package com.sweak.qralarm.features.add_edit_alarm.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.sweak.qralarm.features.add_edit_alarm.main_settings.AddEditAlarmScreen
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmViewModel
import com.sweak.qralarm.features.add_edit_alarm.advanced_settings.AdvancedAlarmSettingsScreen

const val ADD_EDIT_ALARM_FLOW_ROUTE = "addEditAlarmFlow"
const val ID_OF_ALARM_TO_EDIT = "idOfAlarmToEdit"

const val ADD_EDIT_ALARM_SCREEN_ROUTE = "addEditAlarmScreen"
const val ADVANCED_ALARM_SETTINGS_SCREEN_ROUTE = "advancedAlarmSettingsScreen"

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
                onAdvancedSettingsClicked = {
                    navController.navigate(ADVANCED_ALARM_SETTINGS_SCREEN_ROUTE)
                },
                onAlarmDeleted = onAlarmDeleted,
                onRedirectToQRAlarmPro = onRedirectToQRAlarmPro
            )
        }

        composable(route = ADVANCED_ALARM_SETTINGS_SCREEN_ROUTE) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry("$ADD_EDIT_ALARM_FLOW_ROUTE/{$ID_OF_ALARM_TO_EDIT}")
            }
            val addEditAlarmViewModel = hiltViewModel<AddEditAlarmViewModel>(parentEntry)

            AdvancedAlarmSettingsScreen(
                addEditAlarmViewModel = addEditAlarmViewModel,
                onCancelClicked = {
                    navController.navigateUp()
                }
            )
        }
    }
}