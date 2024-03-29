package com.sweak.qralarm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sweak.qralarm.R

val amikoFamily = FontFamily(
    Font(R.font.amiko_regular, FontWeight.Normal),
    Font(R.font.amiko_semibold, FontWeight.SemiBold),
    Font(R.font.amiko_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = White
    ),
    displayMedium = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = White
    ),
    bodyLarge = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = White
    ),
    labelLarge = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = White
    )
)