package com.sweak.qralarm.util

sealed class Screen(val route: String) {
    object AlarmFlow : Screen("alarm_flow")
    object HomeScreen : Screen("home_screen")
    object ScannerScreen : Screen("scanner_screen")
    object MenuScreen : Screen("settings_screen")
}
