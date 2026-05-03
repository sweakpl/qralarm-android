package com.sweak.qralarm.features.onboarding.welcome.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.Gold
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme

@Composable
fun RingingAlarmPhone(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringingAlarmPhone")

    val tilt by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    val burstProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "burstProgress"
    )

    val beamCount = 3
    val staggerFraction = 0.1f
    val expansionFraction = 0.7f

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minRadius = size.minDimension * 0.15f
            val maxRadius = size.minDimension * 0.50f
            val strokeWidth = 2.dp.toPx()

            repeat(beamCount) { index ->
                val beamStart = index * staggerFraction
                val rawProgress = (burstProgress - beamStart) / expansionFraction
                if (rawProgress in 0f..1f) {
                    val easedProgress = EaseOut.transform(rawProgress)
                    drawCircle(
                        color = Gold,
                        radius = lerp(minRadius, maxRadius, easedProgress),
                        alpha = lerp(0.6f, 0f, easedProgress),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.img_ringing_alarm_phone),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .fillMaxHeight()
                .graphicsLayer { rotationZ = tilt }
        )
    }
}

@Preview
@Composable
private fun RingingAlarmPhonePreview() {
    QRAlarmTheme {
        RingingAlarmPhone(modifier = Modifier.fillMaxSize())
    }
}
