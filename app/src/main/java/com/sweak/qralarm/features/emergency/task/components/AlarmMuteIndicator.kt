package com.sweak.qralarm.features.emergency.task.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme

@Composable
fun AlarmMuteIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(modifier = modifier) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            color = color,
            trackColor = Color.Transparent
        )

        Icon(
            imageVector = QRAlarmIcons.SoundMute,
            contentDescription =
                stringResource(R.string.content_description_muted_sound_icon),
            tint = color,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun AlarmMuteIndicatorPreview() {
    QRAlarmTheme {
        AlarmMuteIndicator(progress = 0.65f)
    }
}