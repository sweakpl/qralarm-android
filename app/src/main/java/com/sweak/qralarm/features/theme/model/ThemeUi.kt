package com.sweak.qralarm.features.theme.model

sealed class ThemeUi {
    data object Default : ThemeUi()
    data object Dynamic : ThemeUi()
    data class Custom(val primaryColor: String) : ThemeUi()
}
