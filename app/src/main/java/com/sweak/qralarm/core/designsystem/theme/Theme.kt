package com.sweak.qralarm.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val QRAlarmTheme = darkColorScheme(
    primary = ButterflyBush,
    onPrimary = Color.White,
    surface = Jacarta,
    onSurface = Color.White,
    onSurfaceVariant = Nobel,
    background = BlueZodiac,
    onBackground = Color.White,
    surfaceContainerLow = ButterflyBush,
    surfaceContainerHighest = ButterflyBush,
    error = Monza,
    onError = Color.White
)

val LocalIsQRAlarmTheme = compositionLocalOf { false }

val MaterialTheme.isQRAlarmTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalIsQRAlarmTheme.current

@Composable
fun QRAlarmTheme(content: @Composable () -> Unit) {
    val isUsingQRAlarmTheme = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

    CompositionLocalProvider(
        LocalSpace provides Space(),
        LocalIsQRAlarmTheme provides isUsingQRAlarmTheme
    ) {
        val colorScheme = if (!isUsingQRAlarmTheme) {
            val darkTheme = isSystemInDarkTheme()
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else QRAlarmTheme

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}