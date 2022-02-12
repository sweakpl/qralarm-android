package com.sweak.qralarm

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.home.HomeScreen
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
        NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
            composable(route = Screen.HomeScreen.route) {
                HomeScreen(navController = navController)
            }
            composable(route = Screen.ScannerScreen.route) {
                ScannerScreen()
            }
        }
    }

    private fun switchTimeFormatIfNeeded() {
        runBlocking {
            dataStoreManager.putString(
                DataStoreManager.ALARM_TIME_FORMAT,
                if (DateFormat.is24HourFormat(this@MainActivity)) TimeFormat.MILITARY.name
                else TimeFormat.AMPM.name
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