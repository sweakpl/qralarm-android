package com.sweak.qralarm.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.add_edit_alarm.navigation.addEditAlarmFlow
import com.sweak.qralarm.features.add_edit_alarm.navigation.navigateToAddEditAlarm
import com.sweak.qralarm.features.custom_code_scanner.navigation.customCodeScannerScreen
import com.sweak.qralarm.features.custom_code_scanner.navigation.navigateToCustomCodeScanner
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.disableAlarmScannerScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.navigateToDisableAlarmScanner
import com.sweak.qralarm.features.emergency.settings.navigation.emergencySettingsFlow
import com.sweak.qralarm.features.emergency.settings.navigation.navigateToEmergencySettings
import com.sweak.qralarm.features.emergency.task.navigation.emergencyScreen
import com.sweak.qralarm.features.emergency.task.navigation.navigateToEmergencyScreen
import com.sweak.qralarm.features.home.navigation.HOME_SCREEN_ROUTE
import com.sweak.qralarm.features.home.navigation.homeScreen
import com.sweak.qralarm.features.home.navigation.navigateToHome
import com.sweak.qralarm.features.introduction.navigation.INTRODUCTION_SCREEN_ROUTE
import com.sweak.qralarm.features.introduction.navigation.introductionScreen
import com.sweak.qralarm.features.introduction.navigation.navigateToIntroduction
import com.sweak.qralarm.features.menu.navigation.menuScreen
import com.sweak.qralarm.features.menu.navigation.navigateToMenu
import com.sweak.qralarm.features.optimization.navigation.navigateToOptimization
import com.sweak.qralarm.features.optimization.navigation.optimizationScreen
import com.sweak.qralarm.features.qralarm_pro.navigation.navigateToQRAlarmPro
import com.sweak.qralarm.features.qralarm_pro.navigation.qralarmProScreen
import com.sweak.qralarm.features.rate.navigation.navigateToRate
import com.sweak.qralarm.features.rate.navigation.rateScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { viewModel.state.value.shouldShowSplashScreen }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onEvent(MainActivityUserEvent.ObserveActiveAlarms)
            }
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            ObserveAsEvents(
                flow = viewModel.backendEvents,
                onEvent = { event ->
                    when (event) {
                        is MainActivityBackendEvent.NavigateToActiveAlarm -> {
                            finish()
                            startActivity(
                                Intent(applicationContext, AlarmActivity::class.java).apply {
                                    putExtra(AlarmActivity.EXTRA_ALARM_ID, event.alarmId)
                                    putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, true)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                            )
                        }
                        is MainActivityBackendEvent.ShowRatePrompt -> {
                            navController.navigateToRate()
                        }
                    }
                }
            )

            QRAlarmTheme(useDynamicTheming = state.useDynamicTheming) {
                val isIntroductionFinished = state.isIntroductionFinished

                if (isIntroductionFinished != null) {
                    NavHost(
                        navController = navController,
                        startDestination =
                            if (isIntroductionFinished) HOME_SCREEN_ROUTE
                            else "${INTRODUCTION_SCREEN_ROUTE}/${false}"
                    ) {
                        introductionScreen(
                            onContinueClicked = { wasLaunchedFromMenu ->
                                if (wasLaunchedFromMenu) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigateToHome(
                                        navOptions = navOptions {
                                            popUpTo(
                                                route = "${INTRODUCTION_SCREEN_ROUTE}/${false}",
                                                popUpToBuilder = { inclusive = true }
                                            )
                                        }
                                    )
                                }
                            }
                        )

                        homeScreen(
                            onAddNewAlarm = {
                                navController.navigateToAddEditAlarm()
                            },
                            onEditAlarm = { alarmId ->
                                navController.navigateToAddEditAlarm(alarmId = alarmId)
                            },
                            onMenuClicked = {
                                navController.navigateToMenu()
                            },
                            onRedirectToScanner = { alarmId ->
                                navController.navigateToDisableAlarmScanner(
                                    alarmId = alarmId,
                                    isDisablingBeforeAlarmFired = true
                                )
                            },
                            onRedirectToEmergency = { alarmId ->
                                navController.navigateToEmergencyScreen(alarmIdToCancel = alarmId)
                            },
                            onGoToOptimizationClicked = {
                                navController.navigateToOptimization(isLaunchedFromMenu = false)
                            }
                        )

                        addEditAlarmFlow(
                            navController = navController,
                            onCancelClicked = {
                                navController.navigateUp()
                            },
                            onAlarmSaved = {
                                navController.navigateUp()

                                viewModel.onEvent(MainActivityUserEvent.OnAlarmSaved)
                            },
                            onScanCustomCodeClicked = {
                                navController.navigateToCustomCodeScanner(
                                    shouldScanForDefaultCode = false
                                )
                            },
                            onAlarmDeleted = {
                                navController.popBackStack()
                            },
                            onRedirectToQRAlarmPro = {
                                navController.navigateToQRAlarmPro()
                            }
                        )

                        customCodeScannerScreen(
                            onCustomCodeSaved = {
                                navController.popBackStack()
                            },
                            onCloseClicked = {
                                navController.navigateUp()
                            }
                        )

                        disableAlarmScannerScreen(
                            onAlarmDisabled = {
                                navController.popBackStack()
                            },
                            onCloseClicked = {
                                navController.navigateUp()
                            }
                        )

                        menuScreen(
                            onBackClicked = {
                                navController.navigateUp()
                            },
                            onIntroductionClicked = {
                                navController.navigateToIntroduction(
                                    isLaunchedFromMenu = true
                                )
                            },
                            onOptimizationGuideClicked = {
                                navController.navigateToOptimization(
                                    isLaunchedFromMenu = true
                                )
                            },
                            onEmergencyTaskSettingsClicked = {
                                navController.navigateToEmergencySettings()
                            },
                            onQRAlarmProClicked = {
                                navController.navigateToQRAlarmPro()
                            },
                            onRateQRAlarmClicked = {
                                navController.navigateToRate()
                            },
                            onScanDefaultCodeClicked = {
                                navController.navigateToCustomCodeScanner(
                                    shouldScanForDefaultCode = true
                                )
                            }
                        )

                        optimizationScreen(
                            onBackClicked = {
                                navController.navigateUp()
                            }
                        )

                        emergencySettingsFlow(
                            navController = navController,
                            onBackClicked = {
                                navController.navigateUp()
                            }
                        )

                        qralarmProScreen(
                            onNotNowClicked = {
                                navController.navigateUp()
                            }
                        )

                        rateScreen(
                            onExit = {
                                navController.navigateUp()
                            }
                        )

                        emergencyScreen(
                            onCloseClicked = {
                                navController.navigateUp()
                            },
                            onEmergencyTaskCompleted = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}