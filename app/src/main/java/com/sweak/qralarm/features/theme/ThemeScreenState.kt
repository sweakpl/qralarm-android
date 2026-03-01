package com.sweak.qralarm.features.theme

import com.sweak.qralarm.features.theme.model.ThemeUi
import com.sweak.qralarm.features.theme.util.AVAILABLE_THEMES

data class ThemeScreenState(
    val theme: ThemeUi = ThemeUi.Default,
    val availableCustomThemes: List<ThemeUi.Custom> = AVAILABLE_THEMES
)
