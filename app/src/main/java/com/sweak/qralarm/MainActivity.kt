package com.sweak.qralarm

import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.guide.GuideScreen
import com.sweak.qralarm.ui.screens.home.HomeScreen
import com.sweak.qralarm.ui.screens.scanner.ScannerScreen
import com.sweak.qralarm.ui.screens.settings.SettingsScreen
import com.sweak.qralarm.ui.theme.QRAlarmTheme
import com.sweak.qralarm.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fix for https://issuetracker.google.com/issues/37095334
        window.decorView

        super.onCreate(savedInstanceState)

        val isLockScreenActivity =
            intent.getBooleanExtra(LOCK_SCREEN_VISIBILITY_FLAG, false)

        if (isLockScreenActivity) {
            makeWindowShowOnLockScreen()
        }

        switchTimeFormatIfNeeded()
        checkIfAlarmSet()

        setContent {
            QRAlarmTheme {
                ScreenContent(isLockScreenActivity)
            }
        }
    }

    private fun makeWindowShowOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
    }

    @Composable
    fun ScreenContent(isLockScreenActivity: Boolean) {
        val navController = rememberNavController()
        val isFirstLaunch = runBlocking {
            dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()
        }

        // Fix for blank screen issue on Xiaomi devices:
        // https://issuetracker.google.com/issues/227926002
        ScaffoldDefaults.contentWindowInsets

        NavHost(navController = navController, startDestination = Screen.AlarmFlow.route) {
            navigation(
                startDestination = if (isFirstLaunch) {
                    Screen.GuideScreen.route
                } else {
                    Screen.HomeScreen.route
                },
                route = Screen.AlarmFlow.route
            ) {
                composable(route = Screen.HomeScreen.route) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry(Screen.AlarmFlow.route)
                    }

                    HomeScreen(
                        navController = navController,
                        alarmViewModel = hiltViewModel(parentEntry),
                        finishableActionSideEffect = {
                            if (isLockScreenActivity) finish() else { /* no-op */ }
                        }
                    )
                }
                composable(
                    route = Screen.ScannerScreen.route + "/{$KEY_SCANNER_MODE}",
                    arguments = listOf(
                        navArgument(KEY_SCANNER_MODE) {
                            type = NavType.StringType
                            defaultValue = SCAN_MODE_DISMISS_ALARM
                            nullable = false
                        }
                    )
                ) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry(Screen.AlarmFlow.route)
                    }
                    val acceptAnyCodeType = runBlocking {
                        dataStoreManager.getBoolean(DataStoreManager.ACCEPT_ANY_CODE_TYPE).first()
                    }

                    ScannerScreen(
                        navController = navController,
                        alarmViewModel = hiltViewModel(parentEntry),
                        settingsViewModel = hiltViewModel(parentEntry),
                        acceptAnyCodeType = acceptAnyCodeType,
                        scannerMode = it.arguments?.getString(KEY_SCANNER_MODE),
                        finishableActionSideEffect = {
                            if (isLockScreenActivity) finish() else { /* no-op */ }
                        }
                    )
                }
                composable(route = Screen.SettingsScreen.route) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry(Screen.AlarmFlow.route)
                    }

                    SettingsScreen(
                        navController = navController,
                        settingsViewModel = hiltViewModel(parentEntry)
                    )
                }
                composable(
                    route = Screen.GuideScreen.route,
                ) {
                    GuideScreen(
                        navController = navController,
                        isFirstLaunch = {
                            runBlocking {
                                dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()
                            }
                        },
                        closeGuideCallback = {
                            lifecycleScope.launch {
                                dataStoreManager.putBoolean(DataStoreManager.FIRST_LAUNCH, false)
                            }
                        }
                    )
                }
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