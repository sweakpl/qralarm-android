package com.sweak.qralarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.home.navigation.homeScreen
import com.sweak.qralarm.features.home.navigation.navigateToHome
import com.sweak.qralarm.features.introduction.navigation.INTRODUCTION_SCREEN_ROUTE
import com.sweak.qralarm.features.introduction.navigation.introductionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QRAlarmTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = INTRODUCTION_SCREEN_ROUTE
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

                    homeScreen()
                }
            }
        }
    }
}