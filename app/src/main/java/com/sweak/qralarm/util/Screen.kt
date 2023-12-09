package com.sweak.qralarm.util

sealed class Screen(val route: String) {
    data object AlarmFlow : Screen("alarm_flow")
    data object HomeScreen : Screen("home_screen")
    data object ScannerScreen : Screen("scanner_screen")
    data object SettingsScreen : Screen("settings_screen")
    data object GuideScreen : Screen("guide_screen")

    fun withArguments(vararg arguments: String): String {
        return buildString {
            append(route)
            arguments.forEach { argument ->
                append("/$argument")
            }
        }
    }
}
