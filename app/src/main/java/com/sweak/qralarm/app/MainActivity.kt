package com.sweak.qralarm.app

import android.content.Intent
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
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.RescheduleAlarms
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.add_edit_alarm.navigation.addEditAlarmScreen
import com.sweak.qralarm.features.add_edit_alarm.navigation.navigateToAddEditAlarm
import com.sweak.qralarm.features.custom_code_scanner.navigation.customCodeScannerScreen
import com.sweak.qralarm.features.custom_code_scanner.navigation.navigateToCustomCodeScanner
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.disableAlarmScannerScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.navigateToDisableAlarmScanner
import com.sweak.qralarm.features.home.navigation.HOME_SCREEN_ROUTE
import com.sweak.qralarm.features.home.navigation.homeScreen
import com.sweak.qralarm.features.home.navigation.navigateToHome
import com.sweak.qralarm.features.introduction.navigation.INTRODUCTION_SCREEN_ROUTE
import com.sweak.qralarm.features.introduction.navigation.introductionScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var alarmsRepository: AlarmsRepository
    @Inject lateinit var rescheduleAlarms: RescheduleAlarms

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        var shouldShowSplashScreen by mutableStateOf(true)
        splashScreen.setKeepOnScreenCondition { shouldShowSplashScreen }

        lifecycleScope.launch {
            rescheduleAlarms()
            shouldShowSplashScreen = false

            alarmsRepository.getAllAlarms().collect { alarms ->
                alarms.firstOrNull { alarm ->
                    alarm.isAlarmRunning || alarm.snoozeConfig.isAlarmSnoozed
                }?.let { activeAlarm ->
                    if (activeAlarm.isAlarmRunning && !AlarmService.isRunning) {
                        alarmsRepository.setAlarmRunning(
                            alarmId = activeAlarm.alarmId,
                            running = false
                        )
                        return@let
                    }

                    finish()
                    startActivity(
                        Intent(applicationContext, AlarmActivity::class.java).apply {
                            putExtra(AlarmActivity.EXTRA_ALARM_ID, activeAlarm.alarmId)
                            putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, true)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                }
            }
        }

        val isIntroductionFinished: Boolean
        runBlocking {
            isIntroductionFinished = userDataRepository.isIntroductionFinished.first()
        }

        setContent {
            QRAlarmTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination =
                    if (isIntroductionFinished) HOME_SCREEN_ROUTE else INTRODUCTION_SCREEN_ROUTE
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
                        },
                        onRedirectToScanner = { alarmId ->
                            navController.navigateToDisableAlarmScanner(
                                alarmId = alarmId,
                                isDisablingBeforeAlarmFired = true
                            )
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
                            navController.navigateToCustomCodeScanner()
                        },
                        onAlarmDeleted = {
                            navController.popBackStack()
                        }
                    )

                    customCodeScannerScreen(
                        onCustomCodeSaved = {
                            navController.popBackStack()
                        }
                    )

                    disableAlarmScannerScreen(
                        onAlarmDisabled = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}