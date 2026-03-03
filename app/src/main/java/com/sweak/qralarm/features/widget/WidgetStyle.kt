package com.sweak.qralarm.features.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

object WidgetStyles {

    val title = TextStyle(
        color = ColorProvider(Color.White),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )

    val time = TextStyle(
        color = ColorProvider(Color(0xFFB0BEC5)),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}