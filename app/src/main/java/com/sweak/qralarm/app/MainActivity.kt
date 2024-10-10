package com.sweak.qralarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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