package com.sweak.qralarm.util

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
}
