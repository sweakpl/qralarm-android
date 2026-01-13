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

// Neutral background colors for dynamic theme
private val LightBackground = Color(0xFFF5F5F5)
private val LightBackgroundEnd = Color(0xFFE8E8E8)
private val DarkBackground = Color(0xFF1C1C1C)
private val DarkBackgroundEnd = Color(0xFF121212)
private val LightSurfaceContainer = Color(0xFFFFFFFF)
private val DarkSurfaceContainer = Color(0xFF2D2D2D)
private val White = Color.White
private val Dark = Color(0xFF222222)

@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val dynamicScheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            dynamicScheme.copy(
                primary = if (darkTheme) DarkBackground else LightBackground,
                onPrimary = if (darkTheme) White else Dark,
                secondary = if (darkTheme) DarkBackgroundEnd else LightBackgroundEnd,
                onSecondary = if (darkTheme) White else Dark,
                background = if (darkTheme) DarkBackground else LightBackground,
                surface = if (darkTheme) DarkBackground else LightBackground,
                onSurface = if (darkTheme) White else Dark,
                onSurfaceVariant = if (darkTheme) LightBackgroundEnd else Dark,
                surfaceVariant = if (darkTheme) DarkSurfaceContainer else LightSurfaceContainer,
                surfaceContainer = if (darkTheme) DarkSurfaceContainer else LightSurfaceContainer,
                surfaceContainerLow = if (darkTheme) DarkBackground else LightBackground,
                surfaceContainerHigh = if (darkTheme) DarkSurfaceContainer else LightSurfaceContainer,
                surfaceContainerLowest = if (darkTheme) Dark else White,
                surfaceContainerHighest = if (darkTheme) Dark else LightBackgroundEnd,
                tertiary = dynamicScheme.primary,
                onTertiary = if (darkTheme) Dark else White,
                primaryContainer = dynamicScheme.primaryContainer,
                onPrimaryContainer = dynamicScheme.onPrimaryContainer,
                outline = if (darkTheme) Dark else LightBackgroundEnd,
                outlineVariant = if (darkTheme) Dark else LightBackgroundEnd,
            )
        }
        else -> ColorScheme
    }

    CompositionLocalProvider(LocalSpace provides Space()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}