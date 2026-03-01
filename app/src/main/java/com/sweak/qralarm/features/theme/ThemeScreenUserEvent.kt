package com.sweak.qralarm.features.theme

sealed class ThemeScreenUserEvent {
    data object OnBackClicked : ThemeScreenUserEvent()
    data class OnDynamicThemeToggled(val isChecked: Boolean) : ThemeScreenUserEvent()
    data class OnCustomThemeToggled(val isChecked: Boolean) : ThemeScreenUserEvent()
    data object OnCustomThemeSelected : ThemeScreenUserEvent()
    data object OnColorPickerOpened : ThemeScreenUserEvent()
}
