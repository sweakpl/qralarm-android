package com.sweak.qralarm.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
        val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val darkTheme = isSystemInDarkTheme()
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else ColorScheme // For now ColorScheme will not look good

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}