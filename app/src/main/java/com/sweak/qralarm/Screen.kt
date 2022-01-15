package com.sweak.qralarm

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
}
