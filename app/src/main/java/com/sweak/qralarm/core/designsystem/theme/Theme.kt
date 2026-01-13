package com.sweak.qralarm.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalUseDynamicTheming = staticCompositionLocalOf { true }

val LocalQRAlarmSwitchColors = staticCompositionLocalOf<SwitchColors> {
    error("No SwitchColors provided")
}

val LocalQRAlarmRadioButtonColors = staticCompositionLocalOf<RadioButtonColors> {
    error("No RadioButtonColors provided")
}

@OptIn(ExperimentalMaterial3Api::class)
val LocalQRAlarmTimePickerColors = staticCompositionLocalOf<TimePickerColors> {
    error("No TimePickerColors provided")
}

val LocalAlarmLabelTextColor = staticCompositionLocalOf<Color> {
    error("No AlarmLabelTextColor provided")
}

private val ColorScheme = darkColorScheme(
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
    onError = Color.White,
    outline = Color.White,
    surfaceVariant = Nobel
)

// Neutral background colors for dynamic theme
private val LightBackground = Color(0xFFF5F5F5)
private val LightBackgroundEnd = Color(0xFFE8E8E8)
private val DarkBackground = Color(0xFF1C1C1C)
private val DarkBackgroundEnd = Color(0xFF121212)
private val LightContentColor = Color(0xFFFFFFFF)
private val DarkContentColor = Color(0xFF222222)
private val LightOutlineColor = Color(0xFFCCCCCC)
private val DarkOutlineColor = Color(0xFF3B3B3B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val useDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamicTheming -> {
            val context = LocalContext.current
            val dynamicScheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            dynamicScheme.copy(
                primary = if (darkTheme) DarkBackground else LightBackground,
                onPrimary = if (darkTheme) LightContentColor else DarkContentColor,
                secondary = if (darkTheme) DarkBackgroundEnd else LightBackgroundEnd,
                onSecondary = if (darkTheme) LightContentColor else DarkContentColor,
                primaryContainer = dynamicScheme.primaryContainer,
                onPrimaryContainer = dynamicScheme.onPrimaryContainer,
                background = if (darkTheme) DarkBackground else LightBackground,
                surface = if (darkTheme) DarkBackground else LightBackground,
                onSurface = if (darkTheme) LightContentColor else DarkContentColor,
                onSurfaceVariant = if (darkTheme) LightBackgroundEnd else DarkBackgroundEnd,
                surfaceVariant = if (darkTheme) DarkContentColor else LightContentColor,
                surfaceContainer = if (darkTheme) DarkContentColor else LightContentColor,
                surfaceContainerLow = if (darkTheme) DarkBackground else LightBackground,
                surfaceContainerHigh = if (darkTheme) DarkContentColor else LightContentColor,
                surfaceContainerLowest = if (darkTheme) DarkContentColor else LightContentColor,
                surfaceContainerHighest = if (darkTheme) DarkContentColor else LightContentColor,
                outline = if (darkTheme) DarkBackgroundEnd else LightBackgroundEnd,
                outlineVariant = if (darkTheme) DarkOutlineColor else LightOutlineColor,
                tertiary = dynamicScheme.primary,
                onTertiary = if (darkTheme) DarkContentColor else LightContentColor,
            )
        }
        else -> ColorScheme
    }

    // Compute component colors based on theming mode
    val switchColors = if (useDynamicTheming) {
        SwitchDefaults.colors(
            checkedThumbColor = colorScheme.primary,
            checkedTrackColor = colorScheme.tertiary,
            uncheckedThumbColor = colorScheme.outlineVariant,
            uncheckedBorderColor = colorScheme.outlineVariant,
            uncheckedTrackColor = colorScheme.primary,
        )
    } else {
        SwitchDefaults.colors(
            checkedTrackColor = colorScheme.secondary
        )
    }

    val radioButtonColors = RadioButtonDefaults.colors(
        selectedColor = if (useDynamicTheming) {
            colorScheme.onSurface
        } else {
            colorScheme.secondary
        },
        unselectedColor = colorScheme.onSurface
    )

    val timePickerColors = if (useDynamicTheming) {
        TimePickerDefaults.colors(
            clockDialColor = colorScheme.secondary,
            clockDialSelectedContentColor = colorScheme.onTertiary,
            clockDialUnselectedContentColor = colorScheme.onSecondary,
            selectorColor = colorScheme.tertiary,
            periodSelectorBorderColor = colorScheme.secondary,
            periodSelectorSelectedContainerColor = colorScheme.tertiary,
            periodSelectorSelectedContentColor = colorScheme.onTertiary,
            periodSelectorUnselectedContentColor = colorScheme.onSecondary,
            timeSelectorSelectedContainerColor = colorScheme.tertiary,
            timeSelectorSelectedContentColor = colorScheme.onTertiary,
            timeSelectorUnselectedContainerColor = colorScheme.secondary,
            timeSelectorUnselectedContentColor = colorScheme.onSecondary
        )
    } else {
        // Static theme: all content should be white on purple background
        TimePickerDefaults.colors(
            clockDialColor = colorScheme.onSurface,
            clockDialUnselectedContentColor = colorScheme.primary,
            periodSelectorBorderColor = colorScheme.primary,
            periodSelectorSelectedContainerColor = colorScheme.primary,
            periodSelectorSelectedContentColor = colorScheme.onPrimary,
            periodSelectorUnselectedContentColor = colorScheme.onSurface,
            timeSelectorSelectedContainerColor = colorScheme.primary,
            timeSelectorSelectedContentColor = colorScheme.onPrimary,
            timeSelectorUnselectedContainerColor = colorScheme.tertiary,
            timeSelectorUnselectedContentColor = colorScheme.onTertiary
        )
    }

    // Alarm label text color:
    // - Dynamic theme: dark in light mode, light in dark mode
    // - Static theme: always light (white)
    val alarmLabelTextColor = if (useDynamicTheming) {
        if (darkTheme) LightContentColor else DarkContentColor
    } else {
        LightContentColor
    }

    CompositionLocalProvider(
        LocalUseDynamicTheming provides useDynamicTheming,
        LocalQRAlarmSwitchColors provides switchColors,
        LocalQRAlarmRadioButtonColors provides radioButtonColors,
        LocalQRAlarmTimePickerColors provides timePickerColors,
        LocalAlarmLabelTextColor provides alarmLabelTextColor,
        LocalSpace provides Space()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}