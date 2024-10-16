package com.sweak.qralarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.features.add_edit_alarm.navigation.addEditAlarmScreen
import com.sweak.qralarm.features.add_edit_alarm.navigation.navigateToAddEditAlarm
import com.sweak.qralarm.features.home.navigation.HOME_SCREEN_ROUTE
import com.sweak.qralarm.features.home.navigation.homeScreen
import com.sweak.qralarm.features.home.navigation.navigateToHome
import com.sweak.qralarm.features.introduction.navigation.INTRODUCTION_SCREEN_ROUTE
import com.sweak.qralarm.features.introduction.navigation.introductionScreen
import com.sweak.qralarm.features.scanner.navigation.navigateToScanner
import com.sweak.qralarm.features.scanner.navigation.scannerScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var qrAlarmManager: QRAlarmManager

    @Inject lateinit var alarmsRepository: AlarmsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        var areAlarmsAdjusted by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !areAlarmsAdjusted }

        lifecycleScope.launch {
            if (!qrAlarmManager.canScheduleExactAlarms()) {
                alarmsRepository.getAllAlarms().first().forEach { alarm ->
                    qrAlarmManager.cancelAlarm(alarmId = alarm.alarmId)
                    alarmsRepository.setAlarmEnabled(
                        alarmId = alarm.alarmId,
                        enabled = false
                    )
                }
            } else {
                alarmsRepository.getAllAlarms().first().forEach { alarm ->
                    if (alarm.isAlarmEnabled) {
                        qrAlarmManager.setAlarm(alarmId = alarm.alarmId)
                    }
                }
            }

            areAlarmsAdjusted = true
        }

        setContent {
            QRAlarmTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = HOME_SCREEN_ROUTE
                ) {
                    introductionScreen(
                        onContinueClicked = {
                            navController.navigateToHome(
                                navOptions = navOptions {
                                    popUpTo(
                                        route = INTRODUCTION_SCREEN_ROUTE,
                                        popUpToBuilder = { inclusive = true }
                                    )
                                }
                            )
                        }
                    )

                    homeScreen(
                        onAddNewAlarm = {
                            navController.navigateToAddEditAlarm()
                        },
                        onEditAlarm = { alarmId ->
                            navController.navigateToAddEditAlarm(alarmId = alarmId)
                        }
                    )

                    addEditAlarmScreen(
                        onCancelClicked = {
                            navController.navigateUp()
                        },
                        onAlarmSaved = {
                            navController.navigateUp()
                        },
                        onScanCustomCodeClicked = {
                            navController.navigateToScanner()
                        },
                        onAlarmDeleted = {
                            navController.popBackStack()
                        }
                    )

                    scannerScreen(
                        onCustomCodeSaved = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}