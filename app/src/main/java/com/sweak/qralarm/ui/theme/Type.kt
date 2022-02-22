package com.sweak.qralarm.ui.theme

import androidx.compose.material.Typography
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
    h1 = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = White
    ),
    h2 = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = White
    ),
    body1 = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = White
    ),
    button = TextStyle(
        fontFamily = amikoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = White
    )
)