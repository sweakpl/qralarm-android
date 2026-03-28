package com.sweak.qralarm.features.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

object WidgetStyles {
    val header = TextStyle(
        color = ColorProvider(Color(0xFFFFFFFF)),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    val title = TextStyle(
        color = ColorProvider(Color.White),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )

    val time = TextStyle(
        color = ColorProvider(Color(0xFFFFFFFF)),
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    )
}