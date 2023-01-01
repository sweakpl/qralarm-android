package com.sweak.qralarm.util

sealed class Screen(val route: String) {
    object AlarmFlow : Screen("alarm_flow")
    object HomeScreen : Screen("home_screen")
    object ScannerScreen : Screen("scanner_screen")
    object SettingsScreen : Screen("settings_screen")
    object GuideScreen : Screen("guide_screen")

    fun withArguments(vararg arguments: String): String {
        return buildString {
            append(route)
            arguments.forEach { argument ->
                append("/$argument")
            }
        }
    }
}
