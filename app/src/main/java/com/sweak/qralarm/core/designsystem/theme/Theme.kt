package com.sweak.qralarm.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val ColorScheme = lightColorScheme(
    primary = Jacarta,
    onPrimary = Color.White,
    secondary = BlueZodiac,
    onSecondary = Color.White,
    tertiary = ButterflyBush,
    onTertiary = Color.White,
    surface = ButterflyBush,
    onSurface = Color.White,
    onSurfaceVariant = Nobel,
    error = Monza,
    onError = Color.White
)

@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpace provides Space()) {
        MaterialTheme(
            colorScheme = ColorScheme,
            typography = Typography,
            content = content
        )
    }
}