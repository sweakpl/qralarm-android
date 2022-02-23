package com.sweak.qralarm

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.home.HomeScreen
import com.sweak.qralarm.ui.screens.settings.SettingsScreen
import com.sweak.qralarm.ui.screens.scanner.ScannerScreen
import com.sweak.qralarm.ui.theme.QRAlarmTheme
import com.sweak.qralarm.util.Screen
import com.sweak.qralarm.util.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        switchTimeFormatIfNeeded()
        checkIfAlarmSet()

        setContent {
            QRAlarmTheme {
                ScreenContent()
            }
        }
    }

    @Composable
    fun ScreenContent() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.AlarmFlow.route) {
            navigation(startDestination = Screen.HomeScreen.route, route = Screen.AlarmFlow.route) {
                composable(route = Screen.HomeScreen.route) {
                    val parentEntry = remember {
                        navController.getBackStackEntry(Screen.AlarmFlow.route)
                    }
                    HomeScreen(
                        navController = navController,
                        alarmViewModel = hiltViewModel(parentEntry)
                    )
                }
                composable(route = Screen.ScannerScreen.route) {
                    val parentEntry = remember {
                        navController.getBackStackEntry(Screen.AlarmFlow.route)
                    }
                    ScannerScreen(
                        navController = navController,
                        alarmViewModel = hiltViewModel(parentEntry)
                    )
                }
            }
            composable(route = Screen.MenuScreen.route) {
                SettingsScreen(
                    navController = navController,
                    settingsViewModel = hiltViewModel()
                )
            }
        }
    }

    private fun switchTimeFormatIfNeeded() {
        runBlocking {
            dataStoreManager.putInt(
                DataStoreManager.ALARM_TIME_FORMAT,
                if (DateFormat.is24HourFormat(this@MainActivity)) TimeFormat.MILITARY.ordinal
                else TimeFormat.AMPM.ordinal
            )
        }
    }

    private fun checkIfAlarmSet() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (qrAlarmManager.canScheduleExactAlarms()) {
                    dataStoreManager.putBoolean(
                        DataStoreManager.ALARM_SET,
                        qrAlarmManager.isAlarmSet()
                    )
                } else {
                    qrAlarmManager.removeAlarmPendingIntent()
                    dataStoreManager.putBoolean(DataStoreManager.ALARM_SET, false)
                }
            }
        }
    }
}