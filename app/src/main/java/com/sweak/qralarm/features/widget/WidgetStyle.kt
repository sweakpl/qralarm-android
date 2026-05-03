package com.sweak.qralarm.features.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.sweak.qralarm.core.designsystem.theme.Nobel

private val widgetTextColor = ColorProvider(day = Nobel, night = Nobel)
private val widgetTimeColor = ColorProvider(day = Color.White, night = Color.White)

object WidgetStyles {
    val label = TextStyle(
        color = widgetTextColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif
    )
    val time = TextStyle(
        color = widgetTimeColor,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    )
    val body = TextStyle(
        color = widgetTextColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )
}
