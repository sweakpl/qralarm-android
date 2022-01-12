package com.sweak.qralarm.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val ColorPalette = lightColors(
    primary = Jacarta,
    primaryVariant = BlueZodiac,
    secondary = ButterflyBush,
    secondaryVariant = Victoria,
    onPrimary = White,
    onSecondary = White
)

@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    val colors = ColorPalette

    CompositionLocalProvider(LocalSpace provides Space()) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}