package com.sweak.qralarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

val ColorPalette = lightColorScheme(
    primary = Jacarta,
    onPrimary = White,
    secondary = BlueZodiac,
    onSecondary = White,
    tertiary = ButterflyBush
)

@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpace provides Space()) {
        MaterialTheme(
            colorScheme = ColorPalette,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}