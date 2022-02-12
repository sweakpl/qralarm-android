package com.sweak.qralarm.util

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object ScannerScreen : Screen("scanner_screen")
}
